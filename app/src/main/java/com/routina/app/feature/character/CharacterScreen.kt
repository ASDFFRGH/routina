package com.routina.app.feature.character

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.routina.app.domain.model.CharacterStage
import com.routina.app.ui.components.RoutinaMascot
import com.routina.app.ui.theme.RoutinaTheme

/** Stateless character screen, suitable for previews and UI tests. */
@Composable
fun CharacterScreen(
    uiState: CharacterUiState,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        if (showTitle) {
            Text(
                text = "キャラクター",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CharacterAvatar(stage = uiState.stage)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "レベル ${uiState.level}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    StageBadge(uiState.stage)
                    Text(
                        text = stageDescription(uiState.stage),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        RewardSummary(
            label = "総ポイント",
            value = "${uiState.totalPoints} pt",
        )
        RewardSummary(
            label = "総XP",
            value = "${uiState.totalXp} XP",
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "次のレベルまで ${uiState.xpToNextLevel} XP",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            LinearProgressIndicator(
                progress = { uiState.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "レベル進捗 ${uiState.currentLevelXp} XP / 100 XP"
                    },
            )
            Text(
                text = "${uiState.currentLevelXp} / 100 XP",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun RewardSummary(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.titleMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun CharacterAvatar(stage: CharacterStage) {
    val description = "${stageLabel(stage)}のキャラクター。${stageDescription(stage)}"
    Box(
        modifier = Modifier
            .size(176.dp),
        contentAlignment = Alignment.Center,
    ) {
        RoutinaMascot(contentDescription = description, modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun StageBadge(stage: CharacterStage) {
    val accent = when (stage) {
        CharacterStage.NOVICE -> MaterialTheme.colorScheme.primary
        CharacterStage.ADVENTURER -> MaterialTheme.colorScheme.tertiary
        CharacterStage.MASTER -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    Text(
        text = stageLabel(stage),
        modifier = Modifier
            .background(accent.copy(alpha = .16f), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp),
        color = accent,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
    )
}

private fun stageLabel(stage: CharacterStage): String = when (stage) {
    CharacterStage.NOVICE -> "見習い"
    CharacterStage.ADVENTURER -> "冒険者"
    CharacterStage.MASTER -> "達人"
}

private fun stageDescription(stage: CharacterStage): String = when (stage) {
    CharacterStage.NOVICE -> "小さな一歩を積み重ねています"
    CharacterStage.ADVENTURER -> "挑戦を力に変える冒険者です"
    CharacterStage.MASTER -> "習慣を極めた達人です"
}

@Preview(showBackground = true)
@Composable
private fun CharacterScreenPreview() {
    RoutinaTheme {
        CharacterScreen(
            uiState = CharacterUiState(
                level = 6,
                totalPoints = 1_280,
                totalXp = 520,
                currentLevelXp = 20,
                xpToNextLevel = 80,
                progress = .2f,
                stage = CharacterStage.ADVENTURER,
            ),
        )
    }
}
