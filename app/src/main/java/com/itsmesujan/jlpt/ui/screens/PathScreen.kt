package com.itsmesujan.jlpt.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itsmesujan.jlpt.billing.PremiumFeatures
import com.itsmesujan.jlpt.content.Content
import com.itsmesujan.jlpt.domain.Chapter
import com.itsmesujan.jlpt.domain.LessonNode
import com.itsmesujan.jlpt.ui.AppViewModel
import com.itsmesujan.jlpt.ui.components.KikoMascot
import com.itsmesujan.jlpt.ui.components.StarsRow
import com.itsmesujan.jlpt.ui.components.StatsStrip
import com.itsmesujan.jlpt.ui.components.playerLevel

private val guidebookNotes = mapOf(
    "n5_ch1" to "は marks the topic (pronounced wa). Nouns end sentences with です; add か to ask. の links nouns (にほんのひと). Full formation tables and confused-pair contrasts (は vs が) arrive with the guidebook content pack.",
    "n5_ch2" to "を marks the object of a verb. Object + を + ください is the polite shop request. Prices use number + 円 (えん); counters like 一つ/三つ count things.",
    "n5_ch3" to "に marks specific times and destinations, へ direction, で the place an action happens. Relative times (今日, あした) take no に."
)

/** Home / Path (blueprint screen 3): single Today CTA, then the scrolling quest map. */
@Composable
fun PathScreen(viewModel: AppViewModel, onOpenNode: (LessonNode) -> Unit, onOpenReview: () -> Unit) {
    val profile by viewModel.profile.collectAsState()
    val progress by viewModel.nodeProgress.collectAsState()
    val dueCount by viewModel.dueCount.collectAsState()
    val progressById = progress.associateBy { it.nodeId }
    var guidebookFor by remember { mutableStateOf<String?>(null) }

    val todayNode = Content.chapters.flatMap { it.nodes }.firstOrNull { node ->
        progressById[node.id]?.completed != true && viewModel.isNodeUnlocked(node.id) &&
            PremiumFeatures.canAccessNode(node.premiumOnly, com.itsmesujan.jlpt.billing.Entitlement(viewModel.isPremium()))
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                KikoMascot(Modifier.padding(end = 8.dp))
                Column {
                    Text("JLPT Quest", style = MaterialTheme.typography.headlineMedium)
                    StatsStrip(profile?.streakDays ?: 0, profile?.xp ?: 0, "Lv ${playerLevel(profile?.xp ?: 0)} · ${profile?.targetLevel ?: "N5"}")
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Today", style = MaterialTheme.typography.titleLarge)
                    val line = when {
                        dueCount > 0 && todayNode != null -> "$dueCount reviews due (about ${maxOf(1, dueCount / 4)} min) + Lesson: ${todayNode.title}"
                        dueCount > 0 -> "$dueCount reviews due — clear them to protect your streak."
                        todayNode != null -> "New lesson: ${todayNode.chapterHint()} — ${todayNode.title}"
                        else -> "All available lessons complete. Reviews and the next world are on the way."
                    }
                    Text(line)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { if (dueCount > 0) onOpenReview() else todayNode?.let(onOpenNode) }) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Text(if (dueCount > 0) "Start with reviews" else "Continue")
                    }
                }
            }
        }
        for (chapter in Content.chapters) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text("${chapter.level.label} · ${chapter.title}", style = MaterialTheme.typography.titleLarge)
                        Text(chapter.theme, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(
                        Icons.Filled.MenuBook, contentDescription = "Open guidebook for ${chapter.title}",
                        modifier = Modifier.clickable { guidebookFor = if (guidebookFor == chapter.id) null else chapter.id }.padding(8.dp)
                    )
                }
                if (guidebookFor == chapter.id) {
                    Text(guidebookNotes[chapter.id] ?: "", style = MaterialTheme.typography.bodyMedium)
                }
            }
            items(chapter.nodes) { node -> NodeRow(chapter, node, viewModel, progressById[node.id]?.stars ?: 0, progressById[node.id]?.completed == true, onOpenNode) }
        }
        item {
            Spacer(Modifier.height(8.dp))
            Text("Five Worlds", style = MaterialTheme.typography.titleLarge)
            Text("N5 is open. N4, N3, N2 and N1 arrive as content waves on the same engine — your XP, streak and reviews carry over.")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                Content.lockedLevels.forEach { level ->
                    Card { Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Lock, contentDescription = null)
                        Text(" ${level.label}", style = MaterialTheme.typography.titleMedium)
                    } }
                }
            }
        }
    }
}

private fun LessonNode.chapterHint(): String =
    Content.chapterOf(id)?.title ?: ""

@Composable
private fun NodeRow(
    chapter: Chapter,
    node: LessonNode,
    viewModel: AppViewModel,
    stars: Int,
    completed: Boolean,
    onOpenNode: (LessonNode) -> Unit
) {
    val unlocked = viewModel.isNodeUnlocked(node.id)
    val premiumLocked = node.premiumOnly && !viewModel.isPremium()
    val enabled = unlocked && !premiumLocked
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = enabled) { onOpenNode(node) },
        colors = CardDefaults.cardColors(
            containerColor = when {
                completed -> MaterialTheme.colorScheme.secondaryContainer
                enabled -> MaterialTheme.colorScheme.surface
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(node.kind.title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(node.title, style = MaterialTheme.typography.titleMedium)
                Text(node.subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            when {
                premiumLocked -> Icon(Icons.Filled.Lock, contentDescription = "Premium")
                !unlocked -> Icon(Icons.Filled.Lock, contentDescription = "Locked — finish the previous stop first")
                completed -> StarsRow(stars)
                else -> Icon(Icons.Filled.PlayArrow, contentDescription = "Start")
            }
        }
    }
}
