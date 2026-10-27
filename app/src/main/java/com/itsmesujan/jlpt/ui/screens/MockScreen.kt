package com.itsmesujan.jlpt.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itsmesujan.jlpt.content.Content
import com.itsmesujan.jlpt.domain.ExerciseType
import com.itsmesujan.jlpt.domain.JlptLevel
import com.itsmesujan.jlpt.domain.LessonNode
import com.itsmesujan.jlpt.domain.MockScoring
import com.itsmesujan.jlpt.ui.AppViewModel
import kotlinx.coroutines.delay

/**
 * Mock / boss exam (blueprint screen 8): real timing, NO feedback until the end (exam fidelity).
 * Scoring shows total /180 estimate, section bars and the sectional-minimum trap explicitly.
 * Finishing a mock is paywall moment #2 (POST_MOCK) — the highest-intent moment in the app.
 */
@Composable
fun MockScreen(viewModel: AppViewModel, node: LessonNode, onClose: () -> Unit, onSeePremium: () -> Unit) {
    var index by remember(node.id) { mutableIntStateOf(0) }
    var selected by remember(node.id) { mutableStateOf<String?>(null) }
    var revealed by remember(node.id) { mutableStateOf(false) }
    val answers = remember(node.id) { mutableStateListOf<Pair<com.itsmesujan.jlpt.domain.Exercise, Boolean>>() }
    var remaining by remember(node.id) { mutableIntStateOf(node.timeLimitSeconds ?: 600) }
    var finished by remember(node.id) { mutableStateOf(false) }

    LaunchedEffect(node.id, finished) {
        while (!finished && remaining > 0) { delay(1_000); remaining-- }
        if (remaining == 0 && !finished) finished = true
    }

    if (finished) {
        val level = Content.chapterOf(node.id)?.level ?: JlptLevel.N5
        val result = remember(node.id, answers.size) { MockScoring.score(level, answers.toList()) }
        LaunchedEffect(node.id) {
            val chapterId = Content.chapterOf(node.id)?.id ?: ""
            viewModel.recordSession(node.id, chapterId, answers.toList(), List(answers.size) { 8_000L }) { }
        }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
            Text("Mock results — estimate", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text("Approximation of JLPT scaled scoring; the real conversion is not published. Never a guarantee.")
            Spacer(Modifier.height(16.dp))
            Text("${result.totalScaled} / 180", style = MaterialTheme.typography.headlineLarge, color = if (result.estimatedPass) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            Text("Pass mark for ${level.label}: ${result.overallPassMark} / 180")
            Spacer(Modifier.height(12.dp))
            Text("Language Knowledge & Reading: ${result.languageScaled} / 120 (minimum 38)")
            LinearProgressIndicator(progress = { result.languageScaled / 120f }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Text("Listening: ${result.listeningScaled} / 60 (minimum 19)")
            LinearProgressIndicator(progress = { result.listeningScaled / 60f }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            if (result.failedSectionMinimum != null) {
                Card { Column(Modifier.padding(14.dp)) {
                    Text("Sectional fail", style = MaterialTheme.typography.titleMedium)
                    Text("Your total can pass while one section fails you: ${result.failedSectionMinimum}. The real JLPT works exactly like this.")
                } }
                Spacer(Modifier.height(12.dp))
            }
            Text(if (result.estimatedPass) "Estimated PASS at this level's standard." else "Estimated not-yet — weakest skill: ${result.weakestSkill}. Your weak-spot queue already has these items.")
            Spacer(Modifier.height(20.dp))
            // Paywall moment #2: POST_MOCK — shown only after results, never mid-exam.
            Button(onClick = onSeePremium, modifier = Modifier.fillMaxWidth()) { Text("Fix my weak section with Premium") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text("Back") }
        }
        return
    }

    val exercise = node.exercises[index]
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("${node.title} · ${remaining / 60}:${(remaining % 60).toString().padStart(2, '0')} left · Q${index + 1}/${node.exercises.size}", style = MaterialTheme.typography.titleMedium)
        LinearProgressIndicator(progress = { (index + 1f) / node.exercises.size }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
        exercise.passage?.let { Card { Text(it, Modifier.padding(14.dp), style = MaterialTheme.typography.bodyLarge) }; Spacer(Modifier.height(12.dp)) }
        Text(exercise.prompt, style = MaterialTheme.typography.headlineSmall)
        exercise.promptSub?.let { Text(it) }
        if (exercise.type == ExerciseType.LISTENING) {
            Spacer(Modifier.height(8.dp))
            Button(onClick = { exercise.spokenText?.let { viewModel.speak(it, slow = false) } }) {
                Icon(Icons.Filled.VolumeUp, contentDescription = null); Text(" Play (exam speed)")
            }
            OutlinedButton(onClick = { revealed = true }) { Text("Show text (practice mode)") }
            if (revealed) Text(exercise.spokenText ?: "")
        }
        Spacer(Modifier.height(12.dp))
        exercise.options.forEach { option ->
            OutlinedButton(onClick = { selected = option }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(if (selected == option) "● $option" else option)
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(
            enabled = selected != null,
            onClick = {
                answers.add(exercise to (selected == exercise.correctAnswer))
                selected = null; revealed = false
                if (index + 1 < node.exercises.size) index++ else finished = true
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (index + 1 < node.exercises.size) "Next question" else "Submit mock") }
        Text("No feedback during a mock — that's the point. Explanations unlock on the results screen in the full version.", style = MaterialTheme.typography.bodySmall)
    }
}
