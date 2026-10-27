package com.itsmesujan.jlpt.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.itsmesujan.jlpt.ui.AppViewModel
import com.itsmesujan.jlpt.ui.components.KikoMascot
import com.itsmesujan.jlpt.ui.components.playerLevel

/** Profile / Progress + Settings entry points (blueprint screens 10-11). */
@Composable
fun ProfileScreen(viewModel: AppViewModel, onSeePremium: () -> Unit) {
    val profile by viewModel.profile.collectAsState()
    val cards by viewModel.allCards.collectAsState()
    val progress by viewModel.nodeProgress.collectAsState()
    val context = LocalContext.current
    var showLicences by remember { mutableStateOf(false) }

    val skillCounts = cards.groupingBy { it.skill }.eachCount()
    val achievements = buildList {
        if ((profile?.xp ?: 0) >= 100) add("First 100 XP")
        if ((profile?.streakDays ?: 0) >= 7) add("7-day streak")
        if (progress.any { it.nodeId.endsWith("boss") && it.completed }) add("First boss beaten")
        if (cards.size >= 50) add("50 cards in your deck")
        if (progress.count { it.completed } >= 6) add("Chapter 1 cleared")
    }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                KikoMascot()
                Column(Modifier.padding(start = 12.dp)) {
                    Text("Level ${playerLevel(profile?.xp ?: 0)} · ${profile?.targetLevel ?: "N5"} Adventurer", style = MaterialTheme.typography.titleLarge)
                    Text("${profile?.xp ?: 0} XP · ${profile?.streakDays ?: 0}-day streak · ${profile?.streakFreezes ?: 2} streak freezes")
                }
            }
            val xpInLevel = (profile?.xp ?: 0) % 100
            LinearProgressIndicator(progress = { xpInLevel / 100f }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            Text("$xpInLevel / 100 XP to next level", style = MaterialTheme.typography.bodySmall)
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Mastery by skill", style = MaterialTheme.typography.titleLarge)
                    if (skillCounts.isEmpty()) Text("Finish a lesson and your per-skill deck starts here.")
                    skillCounts.forEach { (skill, count) -> Text("• $skill — $count cards scheduled") }
                    Text("Readiness estimates appear after your first full mock (Practice tab).", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Achievements", style = MaterialTheme.typography.titleLarge)
                    if (achievements.isEmpty()) Text("Your first achievement is one lesson away.")
                    achievements.forEach { Text("🏅 $it") }
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Subscription", style = MaterialTheme.typography.titleLarge)
                    Text(if (viewModel.isPremium()) "Premium active (${profile?.premiumProductId ?: "active"})" else "Free plan — full N5 path included.")
                    Spacer(Modifier.height(8.dp))
                    if (!viewModel.isPremium()) Button(onClick = onSeePremium, modifier = Modifier.fillMaxWidth()) { Text("Go Premium") }
                    OutlinedButton(onClick = { viewModel.restorePurchases() }, modifier = Modifier.fillMaxWidth()) { Text("Restore purchases") }
                    OutlinedButton(
                        onClick = {
                            // Play policy: in-app link to the Play Subscription Centre for management/cancel.
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/account/subscriptions")))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Manage / cancel in Google Play") }
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Settings & legal", style = MaterialTheme.typography.titleLarge)
                    Text("Notifications: 1/day at your chosen time (set in system settings for now). Furigana and font-size controls ship with the content pack update.")
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { showLicences = !showLicences }, modifier = Modifier.fillMaxWidth()) { Text("Licences & attributions") }
                    if (showLicences) Text(
                        "Dictionary data: JMdict/EDICT and KANJIDIC2 (EDRDG) — CC BY-SA 3.0, used for future dictionary expansion; SKIP/frequency fields excluded. Stroke order: KanjiVG (Ulrich Apel) — CC BY-SA 3.0. Example sentences: original for this app; Tatoeba (CC BY 2.0 FR) where credited per item. FSRS scheduling: original implementation informed by the FSRS algorithm (open source). JLPT is a registered trademark of its administrators; this app is not affiliated with the Japan Foundation or JEES, and contains no real JLPT questions.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            // TODO(account): wire to backend account deletion + the web deletion page URL
                            // required by Play policy (in-app path AND web page). Until accounts exist,
                            // all data is local and is removed by clearing app storage / uninstalling.
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Delete account & data") }
                    Text("No account exists yet in this version — all progress is stored on this device only.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
