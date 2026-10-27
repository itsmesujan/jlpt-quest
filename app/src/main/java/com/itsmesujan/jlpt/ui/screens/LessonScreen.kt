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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itsmesujan.jlpt.content.Content
import com.itsmesujan.jlpt.domain.Exercise
import com.itsmesujan.jlpt.domain.ExerciseType
import com.itsmesujan.jlpt.domain.LessonNode
import com.itsmesujan.jlpt.ui.AppViewModel
import com.itsmesujan.jlpt.ui.components.KikoMascot
import com.itsmesujan.jlpt.ui.components.StarsRow

private fun normalise(answer: String): String = answer.trim().lowercase().replace(" ", "")

fun isCorrect(exercise: Exercise, given: String): Boolean =
    exercise.acceptedAnswers.any { normalise(it) == normalise(given) } ||
        normalise(exercise.correctAnswer) == normalise(given)

/**
 * Lesson session (blueprint screen 5): ~5 minutes, progress bar always completes, progress is
 * banked (closing never zeroes a session), instant feedback with WHY after every answer, wrong
 * answers are re-queued once later in the session, and NO hearts are deducted — ever.
 */
@Composable
fun LessonScreen(viewModel: AppViewModel, node: LessonNode, onClose: () -> Unit, onReviewMistakes: () -> Unit) {
    // Working queue: missed items are appended once for in-session retrieval.
    val queue = remember(node.id) { mutableStateListOf<Exercise>().apply { addAll(node.exercises) } }
    var index by remember(node.id) { mutableIntStateOf(0) }
    var selected by remember(node.id) { mutableStateOf<String?>(null) }
    var typed by remember(node.id) { mutableStateOf("") }
    var checked by remember(node.id) { mutableStateOf(false) }
    var lastCorrect by remember(node.id) { mutableStateOf(false) }
    var revealed by remember(node.id) { mutableStateOf(false) }
    var questionStart by remember(node.id) { mutableLongStateOf(System.currentTimeMillis()) }
    val results = remember(node.id) { mutableStateListOf<Pair<Exercise, Boolean>>() }
    val latencies = remember(node.id) { mutableStateListOf<Long>() }
    val requeued = remember(node.id) { mutableStateListOf<String>() }
    var finished by remember(node.id) { mutableStateOf(false) }
    var xpEarned by remember(node.id) { mutableIntStateOf(-1) }

    if (finished) {
        val correct = results.count { it.second }
        val accuracy = if (results.isEmpty()) 0f else correct.toFloat() / results.size
        LaunchedEffect(node.id) { /* results screen is static once shown */ }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            KikoMascot()
            Spacer(Modifier.height(12.dp))
            Text(if (accuracy >= 0.9f) "Quest cleared!" else "Lesson complete", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            StarsRow(when { accuracy >= 0.9f -> 3; accuracy >= 0.7f -> 2; accuracy >= 0.5f -> 1; else -> 0 })
            Spacer(Modifier.height(12.dp))
            Text("$correct of ${results.size} correct · Accuracy ${(accuracy * 100).toInt()}%")
            Text(if (xpEarned >= 0) "+$xpEarned XP" else "Saving…", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Text("Missed items went to your weak-spot deck and FSRS reviews — they'll come back until they stick.", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(20.dp))
            if (results.any { !it.second }) {
                Button(onClick = onReviewMistakes, modifier = Modifier.fillMaxWidth()) { Text("Review mistakes") }
                Spacer(Modifier.height(8.dp))
            }
            OutlinedButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text("Back to path") }
        }
        return
    }

    val exercise = queue[index]
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = "Close lesson (progress is banked)") }
            LinearProgressIndicator(progress = { index.toFloat() / queue.size }, modifier = Modifier.weight(1f))
            Text("  ${index + 1}/${queue.size}", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.height(8.dp))
        Text(node.kind.title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

        exercise.passage?.let { passage ->
            Card { Text(passage, Modifier.padding(14.dp), style = MaterialTheme.typography.bodyLarge) }
            Spacer(Modifier.height(12.dp))
        }
        Text(exercise.prompt, style = MaterialTheme.typography.headlineSmall)
        exercise.promptSub?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Spacer(Modifier.height(12.dp))

        if (exercise.type == ExerciseType.LISTENING) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { exercise.spokenText?.let { viewModel.speak(it) } }) {
                    Icon(Icons.Filled.VolumeUp, contentDescription = null); Text(" Listen")
                }
                TextButton(onClick = { exercise.spokenText?.let { viewModel.speak(it, slow = true) } }) { Text("Slow") }
                TextButton(onClick = { revealed = true }) { Text("Can't listen? Show it") }
            }
            if (revealed) Text(exercise.spokenText ?: "", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
        }

        when (exercise.type) {
            ExerciseType.TYPED_RETRIEVAL -> OutlinedTextField(
                value = typed, onValueChange = { typed = it },
                label = { Text("Type your answer") }, singleLine = true,
                enabled = !checked, modifier = Modifier.fillMaxWidth()
            )
            else -> exercise.options.forEach { option ->
                OutlinedButton(
                    onClick = { if (!checked) selected = option },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) { Text(if (selected == option) "● $option" else option) }
            }
        }

        Spacer(Modifier.height(16.dp))
        if (!checked) {
            val canCheck = if (exercise.type == ExerciseType.TYPED_RETRIEVAL) typed.isNotBlank() else selected != null
            Button(
                enabled = canCheck,
                onClick = {
                    val given = if (exercise.type == ExerciseType.TYPED_RETRIEVAL) typed else selected.orEmpty()
                    lastCorrect = isCorrect(exercise, given)
                    results.add(exercise to lastCorrect)
                    latencies.add(System.currentTimeMillis() - questionStart)
                    if (!lastCorrect && exercise.id !in requeued) { queue.add(exercise); requeued.add(exercise.id) }
                    checked = true
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Check") }
        } else {
            Card(colors = CardDefaults.cardColors(containerColor = if (lastCorrect) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer)) {
                Column(Modifier.padding(14.dp)) {
                    Text(if (lastCorrect) "Correct!" else "Not quite — here's the fix:")
                    if (!lastCorrect) Text("Answer: ${exercise.correctAnswer}", style = MaterialTheme.typography.titleMedium)
                    Text(exercise.explanation, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    val wasLast = index + 1 >= queue.size
                    if (wasLast) {
                        finished = true
                        val chapterId = Content.chapterOf(node.id)?.id ?: ""
                        viewModel.recordSession(node.id, chapterId, results.toList(), latencies.toList()) { xp -> xpEarned = xp }
                    } else {
                        index++; selected = null; typed = ""; checked = false; revealed = false
                        questionStart = System.currentTimeMillis()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (index + 1 >= queue.size) "Finish" else "Continue") }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { /* TODO(content): per-exercise issue report queue (blueprint QA pipeline) */ }) {
                Text("Report an issue")
            }
        }
    }
}
