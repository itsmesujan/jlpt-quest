package com.itsmesujan.jlpt.ui

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.itsmesujan.jlpt.billing.BillingManager
import com.itsmesujan.jlpt.billing.Entitlement
import com.itsmesujan.jlpt.data.local.MistakeEntity
import com.itsmesujan.jlpt.data.local.NodeProgressEntity
import com.itsmesujan.jlpt.data.local.PlayerProfileEntity
import com.itsmesujan.jlpt.data.local.ReviewCardEntity
import com.itsmesujan.jlpt.data.local.todayEpochDay
import com.itsmesujan.jlpt.domain.Exercise
import com.itsmesujan.jlpt.domain.Rating
import com.itsmesujan.jlpt.tts.JapaneseTts
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as com.itsmesujan.jlpt.JlptQuestApplication
    val repository = app.repository

    val tts = JapaneseTts(application)

    val billing = BillingManager(application) { entitlement ->
        viewModelScope.launch { repository.setPremium(entitlement.isPremium, entitlement.productId, entitlement.purchaseToken) }
    }

    val profile: StateFlow<PlayerProfileEntity?> = repository.profile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val nodeProgress: StateFlow<List<NodeProgressEntity>> = repository.nodeProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val dueCards: StateFlow<List<ReviewCardEntity>> = repository.dueCards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val dueCount: StateFlow<Int> = repository.dueCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
    val allCards: StateFlow<List<ReviewCardEntity>> = repository.allCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val openMistakes: StateFlow<List<MistakeEntity>> = repository.openMistakes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val products: StateFlow<List<ProductDetails>> = billing.products
    val entitlement: StateFlow<Entitlement> = billing.entitlement

    init {
        viewModelScope.launch { repository.ensureProfile() }
        billing.connect()
    }

    fun isPremium(): Boolean = profile.value?.isPremium == true || entitlement.value.isPremium

    fun completeOnboarding(goal: String, level: String, dailyMinutes: Int) {
        viewModelScope.launch { repository.completeOnboarding(goal, level, dailyMinutes) }
    }

    fun speak(text: String, slow: Boolean = false) = tts.speak(text, slow)

    /** Record a finished session; invokes [onXp] with earned XP for the results screen. */
    fun recordSession(nodeId: String, chapterId: String, results: List<Pair<Exercise, Boolean>>, latenciesMs: List<Long>, onXp: (Int) -> Unit) {
        viewModelScope.launch { onXp(repository.recordSession(nodeId, chapterId, results, latenciesMs)) }
    }

    fun recordReview(card: ReviewCardEntity, rating: Rating) {
        viewModelScope.launch { repository.recordReview(card, rating) }
    }

    fun buy(activity: Activity, product: ProductDetails, offerToken: String? = null) =
        billing.launchPurchase(activity, product, offerToken)

    fun restorePurchases() = billing.refresh()

    /** Node unlock rule: nodes unlock sequentially inside a chapter; a chapter unlocks when the
     *  previous chapter's boss is completed. Premium nodes additionally require entitlement. */
    fun isNodeUnlocked(nodeId: String): Boolean {
        val chapters = com.itsmesujan.jlpt.content.Content.chapters
        val progressById = nodeProgress.value.associateBy { it.nodeId }
        for ((chapterIndex, chapter) in chapters.withIndex()) {
            if (chapterIndex > 0) {
                val prevBoss = chapters[chapterIndex - 1].nodes.last()
                if (progressById[prevBoss.id]?.completed != true) {
                    if (chapter.nodes.any { it.id == nodeId }) return false
                }
            }
            val indexInChapter = chapter.nodes.indexOfFirst { it.id == nodeId }
            if (indexInChapter < 0) continue
            if (indexInChapter == 0) return true
            val previous = chapter.nodes[indexInChapter - 1]
            return progressById[previous.id]?.completed == true
        }
        return false
    }

    fun today(): Long = todayEpochDay()

    override fun onCleared() {
        tts.shutdown()
        billing.disconnect()
        super.onCleared()
    }
}
