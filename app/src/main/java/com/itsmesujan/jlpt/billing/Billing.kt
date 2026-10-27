package com.itsmesujan.jlpt.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Product IDs — must match Play Console EXACTLY (Console: Monetize > Products).
 * Prices (US, blueprint): monthly $7.99, annual $47.99 with a 7-day free trial offer,
 * lifetime $179. Trial/intro offers are configured in Play Console, never in code.
 * All prices are fetched from Play at runtime and rendered from ProductDetails —
 * never hard-code a price string in UI.
 */
object ProductIds {
    const val PREMIUM_MONTHLY = "premium_monthly"
    const val PREMIUM_YEARLY = "premium_yearly"
    const val PREMIUM_LIFETIME = "premium_lifetime" // one-time (non-consumable) product
    val SUBSCRIPTIONS = listOf(PREMIUM_MONTHLY, PREMIUM_YEARLY)
    val INAPP = listOf(PREMIUM_LIFETIME)
}

/** Paywall entry points (blueprint order). A paywall is NEVER shown mid-lesson or after a failed exercise. */
enum class PaywallMoment { POST_PLACEMENT, POST_MOCK, AT_LIMIT, DAY3_RECAP, DAY7_RECAP, SETTINGS }

data class Entitlement(val isPremium: Boolean = false, val productId: String? = null, val purchaseToken: String? = null)

/**
 * Feature-flag / entitlement layer. Every premium gate in the UI reads [BillingManager.entitlement]
 * (mirrored into Room by the ViewModel) — screens never query BillingClient directly.
 */
object PremiumFeatures {
    const val FREE_DAILY_NEW_ITEMS = 10          // free tier daily new-item cap (blueprint)
    const val FREE_MOCKS_PER_LEVEL = 1
    fun canAccessNode(premiumOnly: Boolean, entitlement: Entitlement) = !premiumOnly || entitlement.isPremium
    fun canStartMock(alreadyUsedFreeMock: Boolean, entitlement: Entitlement) = entitlement.isPremium || !alreadyUsedFreeMock
}

/**
 * Google Play Billing Library 9.x wrapper.
 *
 * Purchase flow: connect -> queryProductDetails -> launchBillingFlow -> onPurchasesUpdated ->
 * verify (SERVER TODO) -> acknowledge within 3 days (unacknowledged purchases auto-refund).
 */
class BillingManager(
    context: Context,
    private val onEntitlementChanged: (Entitlement) -> Unit
) : PurchasesUpdatedListener {

    private val appContext = context.applicationContext
    private val billingClient: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection()
        .build()

    private val _products = MutableStateFlow<List<ProductDetails>>(emptyList())
    val products: StateFlow<List<ProductDetails>> = _products

    private val _entitlement = MutableStateFlow(Entitlement())
    val entitlement: StateFlow<Entitlement> = _entitlement

    fun connect() {
        if (billingClient.isReady) { refresh(); return }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) refresh()
            }
            override fun onBillingServiceDisconnected() {
                // Auto-service-reconnection is enabled; also safe to call connect() again from UI retry.
            }
        })
    }

    fun refresh() {
        queryProducts()
        restorePurchases()
    }

    private fun queryProducts() {
        val subParams = QueryProductDetailsParams.newBuilder()
            .setProductList(ProductIds.SUBSCRIPTIONS.map {
                QueryProductDetailsParams.Product.newBuilder().setProductId(it).setProductType(BillingClient.ProductType.SUBS).build()
            }).build()
        billingClient.queryProductDetailsAsync(subParams) { result, detailsResult ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                _products.value = _products.value + detailsResult.productDetailsList
            }
        }
        val inappParams = QueryProductDetailsParams.newBuilder()
            .setProductList(ProductIds.INAPP.map {
                QueryProductDetailsParams.Product.newBuilder().setProductId(it).setProductType(BillingClient.ProductType.INAPP).build()
            }).build()
        billingClient.queryProductDetailsAsync(inappParams) { result, detailsResult ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                _products.value = _products.value + detailsResult.productDetailsList
            }
        }
    }

    /** Launch the purchase flow for a product. For subscriptions, pass the chosen offer token
     *  (e.g. the 7-day-trial offer on the yearly plan) from the paywall UI. */
    fun launchPurchase(activity: Activity, productDetails: ProductDetails, offerToken: String? = null) {
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .apply { if (offerToken != null) setOfferToken(offerToken) }
            .build()
        val flowParams = BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(productParams)).build()
        billingClient.launchBillingFlow(activity, flowParams)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> purchases?.forEach { handlePurchase(it) }
            BillingClient.BillingResponseCode.USER_CANCELED -> { /* silent: user dismissed the sheet */ }
            else -> { /* TODO(analytics): log billing error code for paywall funnel diagnosis */ }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        // TODO(server-verification): POST purchase.purchaseToken + productId to your backend and
        // verify with the Google Play Developer API (or RevenueCat) BEFORE granting entitlement in
        // production. Client-only granting below is acceptable for closed testing only — a tampered
        // client can fake it. Keep the token so the backend can re-verify on restore.
        grantEntitlement(purchase)
        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
            billingClient.acknowledgePurchase(params) { /* result ignored; Play retries on restore pass */ }
        }
    }

    /** Restore: re-query owned purchases (subscriptions + lifetime) and re-apply entitlement. */
    fun restorePurchases() {
        val subsParams = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        billingClient.queryPurchasesAsync(subsParams) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) purchases.forEach { handlePurchase(it) }
        }
        val inappParams = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        billingClient.queryPurchasesAsync(inappParams) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) purchases.forEach { handlePurchase(it) }
        }
    }

    private fun grantEntitlement(purchase: Purchase) {
        val entitlement = Entitlement(isPremium = true, productId = purchase.products.firstOrNull(), purchaseToken = purchase.purchaseToken)
        _entitlement.value = entitlement
        onEntitlementChanged(entitlement)
    }

    fun disconnect() = billingClient.endConnection()
}
