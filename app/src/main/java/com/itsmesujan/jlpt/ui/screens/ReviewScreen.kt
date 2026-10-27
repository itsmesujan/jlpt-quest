package com.itsmesujan.jlpt.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itsmesujan.jlpt.content.Content
import com.itsmesujan.jlpt.data.local.ReviewCardEntity
import com.itsmesujan.jlpt.domain.Rating
import com.itsmesujan.jlpt.ui.AppViewModel

/**
 * Practice hub + Review session (blueprint screens 6-7): FSRS queue with forecast, weak-spot
 * deck from mistakes, kanji/mock shortcuts. Review cards offer explicit 4-button rating here
 * (in lessons, ratings are inferred instead — see FsrsScheduler.inferRating).
 */
@Composable
fun ReviewScreen(
    viewModel: AppViewModel,
    onOpenMock: (String) -> Unit,
    onOpenPremium: () -> Unit
) {
    val dueCards by viewModel.dueCards.collectAsState()
    val allCards by viewModel.allCards.collectAsState()
    val mistakes by viewModel.openMistakes.collectAsState()
    val profile by viewModel.profile.collectAsState()

    // Session queue: due cards first, then any card tied to an open mistake.
    val mistakeKeys = mistakes.map { it.itemKey }.toSet()
    val sessionQueue = remember(dueCards, allCards, mistakes) {
        (dueCards + allCards.filter { it.itemKey in mistakeKeys }).distinctBy { it.itemKey }
    }
    var sessionIndex by remember { mutableIntStateOf(-1) } // -1 = hub view
    var revealed by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Practice", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))

        if (sessionIndex in sessionQueue.indices) {
            val card = sessionQueue[sessionIndex]
            Text("Review ${sessionIndex + 1} of ${sessionQueue.size}", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(card.prompt, style = MaterialTheme.typography.headlineSmall)
                    card.promptSub?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    if (revealed) {
                        Spacer(Modifier.height(12.dp))
                        Text(card.correctAnswer, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        Text(card.explanation, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            if (!revealed) {
                Button(onClick = { revealed = true }, modifier = Modifier.fillMaxWidth()) { Text("Show answer") }
            } else {
                Text("How well did you know it?", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf(Rating.AGAIN to "Again", Rating.HARD to "Hard", Rating.GOOD to "Good", Rating.EASY to "Easy").forEach { (rating, label) ->
                        OutlinedButton(
                            onClick = {
                                viewModel.recordReview(card, rating)
                                revealed = false
                                sessionIndex = if (sessionIndex + 1 < sessionQueue.size) sessionIndex + 1 else -1
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text(label) }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { sessionIndex = -1 }, modifier = Modifier.fillMaxWidth()) { Text("End review (progress saved)") }
            return@Column
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("FSRS reviews", style = MaterialTheme.typography.titleLarge)
                Text("${dueCards.size} due now · ${allCards.size} cards in your deck")
                val tomorrow = allCards.count { it.dueEpochDay == viewModel.today() + 1 }
                Text("Forecast: about $tomorrow due tomorrow. Daily reviews are capped by scheduling — no avalanche, ever.")
                Spacer(Modifier.height(8.dp))
                Button(enabled = sessionQueue.isNotEmpty(), onClick = { sessionIndex = 0 }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (sessionQueue.isEmpty()) "All caught up" else "Start review")
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Weak spots", style = MaterialTheme.typography.titleLarge)
                Text(if (mistakes.isEmpty()) "No open weak spots. Missed items land here automatically." else "${mistakes.size} missed item(s) waiting — they're mixed into your review queue above.")
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Boss fights & mocks", style = MaterialTheme.typography.titleLarge)
        Text("Timed mini-mocks per chapter. Premium unlocks full timed mocks with sectional diagnosis.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(Content.chapters.flatMap { it.nodes }.filter { it.isBoss }) { boss ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(boss.title, style = MaterialTheme.typography.titleMedium)
                            Text(boss.subtitle, style = MaterialTheme.typography.bodyMedium)
                        }
                        Button(onClick = { onOpenMock(boss.id) }) { Text("Start") }
                    }
                }
            }
            item {
                OutlinedButton(onClick = onOpenPremium, modifier = Modifier.fillMaxWidth()) { Text("Unlock full mocks with Premium") }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text("Kanji tracing (KanjiVG stroke order), shadowing and graded reader arrive as Practice upgrades — see README roadmap.", style = MaterialTheme.typography.bodySmall)
        @Suppress("UNUSED_EXPRESSION") profile // profile kept for streak-freeze display in v1.1
    }
}
