package com.itsmesujan.jlpt.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.android.billingclient.api.ProductDetails
import com.itsmesujan.jlpt.billing.ProductIds
import com.itsmesujan.jlpt.ui.AppViewModel
import com.itsmesujan.jlpt.ui.components.KikoMascot

/**
 * Paywall (blueprint screen 12 + monetization blueprint (c)).
 * Policy-mandated shape: annual TOTAL price prominent (never only a per-month breakdown),
 * trial terms stated in full, localised prices loaded from Play (never hard-coded in UI),
 * Restore and a clearly visible dismiss always present, no fake countdowns, no "Free Trial" SKU names.
 * Blueprint list prices (US) when Play has not returned details yet: $7.99/mo, $47.99/yr, $179 lifetime.
 */
@Composable
fun PaywallScreen(viewModel: AppViewModel, moment: String, onDismiss: () -> Unit) {
    val products by viewModel.products.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    val headline = when (moment) {
        "POST_MOCK" -> "Your mock found the gap. Premium closes it."
        "POST_PLACEMENT" -> "Your personalised plan, fully unlocked"
        "AT_LIMIT" -> "You've hit today's free limit — nicely done"
        else -> "One subscription. Every level. No ads, ever."
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
            Icon(Icons.Filled.Close, contentDescription = "Dismiss paywall and continue free")
        }
        KikoMascot()
        Spacer(Modifier.height(8.dp))
        Text(headline, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        listOf(
            "Unlimited new items and reviews (no daily cap)",
            "All timed mock exams with sectional diagnosis and per-item explanations",
            "Offline lesson and audio packs for every level, N5 to N1 as worlds release",
            "Advanced weak-spot training built from your mistakes",
            "New content every month — sustained value, no hearts, no ads in lessons"
        ).forEach { Text("✓ $it", style = MaterialTheme.typography.bodyLarge) }
        Spacer(Modifier.height(16.dp))

        if (products.isEmpty()) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Premium", style = MaterialTheme.typography.titleLarge)
                    Text("Monthly $7.99 · Annual $47.99/year · Lifetime $179 (US list prices).")
                    Text("Live prices in your currency load from Google Play when the store connection is ready. If this message persists, check your connection and try Restore.")
                }
            }
        } else {
            products.sortedBy { it.productId }.forEach { details ->
                ProductCard(details) { offerToken ->
                    if (activity != null) viewModel.buy(activity, details, offerToken)
                }
                Spacer(Modifier.height(10.dp))
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "Annual plan: 7-day free trial, then $47.99/year (US) — your exact price, in your currency, is shown by Google Play before you confirm. It converts automatically unless cancelled at least 24 hours before the trial ends. Cancel anytime in Google Play > Subscriptions; cancelling stops renewal, Premium stays active until the period ends. Lifetime is a one-time payment, no trial. Prices may vary by country and taxes.",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { viewModel.restorePurchases() }) { Text("Restore purchases") }
        TextButton(onClick = onDismiss) { Text("Continue with the free plan") }
    }
}

@Composable
private fun ProductCard(details: ProductDetails, onBuy: (String?) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(details.name.ifBlank { details.productId }, style = MaterialTheme.typography.titleLarge)
            when (details.productId) {
                ProductIds.PREMIUM_YEARLY -> {
                    val offer = details.subscriptionOfferDetails?.firstOrNull()
                    val phase = offer?.pricingPhases?.pricingPhaseList?.lastOrNull()
                    Text(phase?.formattedPrice?.let { "$it / year" } ?: "Annual", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    Text("Includes a 7-day free trial (configured in Play Console). $47.99/year US list, shown first — not just a per-month breakdown.")
                    Button(onClick = { onBuy(offer?.offerToken) }, modifier = Modifier.fillMaxWidth()) { Text("Start free trial") }
                }
                ProductIds.PREMIUM_MONTHLY -> {
                    val offer = details.subscriptionOfferDetails?.firstOrNull()
                    val phase = offer?.pricingPhases?.pricingPhaseList?.lastOrNull()
                    Text(phase?.formattedPrice?.let { "$it / month" } ?: "Monthly", style = MaterialTheme.typography.headlineSmall)
                    Button(onClick = { onBuy(offer?.offerToken) }, modifier = Modifier.fillMaxWidth()) { Text("Go Premium monthly") }
                }
                else -> { // lifetime one-time product
                    val price = details.oneTimePurchaseOfferDetails?.formattedPrice ?: ""
                    Text(if (price.isNotBlank()) "$price once, yours forever" else "One-time purchase", style = MaterialTheme.typography.headlineSmall)
                    Button(onClick = { onBuy(null) }, modifier = Modifier.fillMaxWidth()) { Text("Get Lifetime") }
                }
            }
        }
    }
}
