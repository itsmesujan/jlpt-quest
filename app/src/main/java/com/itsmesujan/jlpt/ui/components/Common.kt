package com.itsmesujan.jlpt.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.itsmesujan.jlpt.R

@Composable
fun KikoMascot(modifier: Modifier = Modifier.size(72.dp)) {
    Image(
        painter = painterResource(id = R.drawable.kiko_mascot),
        contentDescription = "Kiko the kitsune, your study companion",
        modifier = modifier
    )
}

@Composable
fun StarsRow(stars: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier.semantics { contentDescription = "$stars of 3 stars earned" }) {
        repeat(3) { index ->
            Icon(
                imageVector = if (index < stars) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/** Top stats strip used on Path: streak (with flame), XP total, level label. */
@Composable
fun StatsStrip(streakDays: Int, xp: Int, levelLabel: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(Icons.Filled.Whatshot, contentDescription = "Streak", tint = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.width(4.dp))
        Text("$streakDays", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(16.dp))
        Text("XP $xp", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(16.dp))
        Text(levelLabel, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
    }
}

/** Player level from total XP: 100 XP per level, uncapped. */
fun playerLevel(xp: Int): Int = xp / 100 + 1
