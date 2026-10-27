package com.itsmesujan.jlpt.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itsmesujan.jlpt.content.Content
import com.itsmesujan.jlpt.ui.AppViewModel
import com.itsmesujan.jlpt.ui.components.KikoMascot

/**
 * Onboarding (<90 seconds to first success, blueprint screen 1-2):
 * goal -> daily goal -> 12-item adaptive placement -> result card -> soft paywall hook.
 * No account wall, no card-required trial. "Start from zero" is always offered.
 */
@Composable
fun OnboardingScreen(viewModel: AppViewModel, onDone: () -> Unit, onSeePremium: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var goal by remember { mutableStateOf("JLPT exam") }
    var dailyMinutes by remember { mutableIntStateOf(10) }
    var placementIndex by remember { mutableIntStateOf(0) }
    var placementCorrect by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        KikoMascot()
        Spacer(Modifier.height(12.dp))
        when (step) {
            0 -> {
                Text("Your Japanese quest starts here", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(8.dp))
                Text("I'm Kiko. Tell me why you're learning and I'll build your path to the JLPT.")
                Spacer(Modifier.height(16.dp))
                Text("Your goal", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("JLPT exam", "Live in Japan", "Work in Japan", "Anime & culture").forEach { option ->
                        FilterChip(selected = goal == option, onClick = { goal = option }, label = { Text(option) })
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Daily goal", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 10, 15).forEach { minutes ->
                        FilterChip(selected = dailyMinutes == minutes, onClick = { dailyMinutes = minutes }, label = { Text("$minutes min — I can do this") })
                    }
                }
                Spacer(Modifier.height(24.dp))
                Button(onClick = { step = 1 }, modifier = Modifier.fillMaxWidth()) { Text("Take the 3-minute placement") }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { viewModel.completeOnboarding(goal, "N5", dailyMinutes); onDone() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Start from zero instead") }
            }
            1 -> {
                val items = Content.placement
                val exercise = items[placementIndex]
                Text("Placement ${placementIndex + 1} of ${items.size}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(progress = { (placementIndex + 1f) / items.size }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                Text(exercise.prompt, style = MaterialTheme.typography.headlineSmall)
                exercise.promptSub?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Spacer(Modifier.height(12.dp))
                exercise.options.forEach { option ->
                    OutlinedButton(
                        onClick = { selected = option },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) { Text(if (selected == option) "● $option" else option) }
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    enabled = selected != null,
                    onClick = {
                        if (selected == exercise.correctAnswer) placementCorrect++
                        selected = null
                        if (placementIndex + 1 < items.size) placementIndex++ else step = 2
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Next") }
            }
            else -> {
                val total = Content.placement.size
                val startHint = when {
                    placementCorrect >= 10 -> "Strong start — you already know kana, core words and particles. Start at N5 Chapter 1 and speed-run it; Chapter 2 will challenge you."
                    placementCorrect >= 6 -> "You know the basics. Start at N5 Chapter 1 — the first nodes will feel like a warm-up."
                    else -> "Perfect starting point. N5 Chapter 1 assumes nothing and teaches everything from zero."
                }
                Text("Your path is ready", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(8.dp))
                Text("Placement: $placementCorrect of $total correct (estimate — your real level sharpens as you play).")
                Spacer(Modifier.height(8.dp))
                Text(startHint)
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.completeOnboarding(goal, "N5", dailyMinutes); onDone() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Start my quest — free") }
                Spacer(Modifier.height(8.dp))
                // Paywall moment #1: POST_PLACEMENT soft paywall — dismissible, free path above is primary.
                OutlinedButton(onClick = onSeePremium, modifier = Modifier.fillMaxWidth()) {
                    Text("See what Premium adds (optional)")
                }
            }
        }
    }
}
