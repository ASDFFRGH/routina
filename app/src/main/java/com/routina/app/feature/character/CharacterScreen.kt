package com.routina.app.feature.character

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.routina.app.domain.model.CharacterStage
import com.routina.app.ui.theme.RoutinaTheme

/** Stateless character screen, suitable for previews and UI tests. */
@Composable
fun CharacterScreen(
    uiState: CharacterUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "キャラクター",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                CharacterAvatar(stage = uiState.stage)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "レベル ${uiState.level}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stageLabel(uiState.stage),
                        style = MaterialTheme.typography.titleMedium,
                    )
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
            .size(108.dp)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val bodyTop = size.height * .43f
            val headRadius = size.width * .18f
            val bodyColor = when (stage) {
                CharacterStage.NOVICE -> Color(0xFF4E7D8A)
                CharacterStage.ADVENTURER -> Color(0xFF5D5AA7)
                CharacterStage.MASTER -> Color(0xFF8A5A18)
            }
            val accentColor = when (stage) {
                CharacterStage.NOVICE -> Color(0xFFB9E3EC)
                CharacterStage.ADVENTURER -> Color(0xFFFFC857)
                CharacterStage.MASTER -> Color(0xFFFFE08A)
            }

            drawCircle(color = bodyColor.copy(alpha = .16f), radius = size.minDimension / 2)
            drawCircle(color = accentColor, radius = headRadius, center = Offset(centerX, size.height * .28f))
            drawRoundRect(
                color = bodyColor,
                topLeft = Offset(size.width * .30f, bodyTop),
                size = androidx.compose.ui.geometry.Size(size.width * .40f, size.height * .38f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.width * .12f),
            )

            when (stage) {
                CharacterStage.NOVICE -> {
                    drawCircle(Color.White, radius = size.width * .035f, center = Offset(size.width * .44f, size.height * .28f))
                    drawCircle(Color.White, radius = size.width * .035f, center = Offset(size.width * .56f, size.height * .28f))
                }
                CharacterStage.ADVENTURER -> {
                    drawLine(
                        color = accentColor,
                        start = Offset(size.width * .72f, size.height * .68f),
                        end = Offset(size.width * .88f, size.height * .35f),
                        strokeWidth = size.width * .055f,
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(size.width * .80f, size.height * .56f),
                        end = Offset(size.width * .92f, size.height * .62f),
                        strokeWidth = size.width * .04f,
                    )
                }
                CharacterStage.MASTER -> {
                    val crownY = size.height * .12f
                    drawLine(accentColor, Offset(size.width * .34f, crownY + 12f), Offset(size.width * .40f, crownY - 4f), size.width * .05f)
                    drawLine(accentColor, Offset(size.width * .40f, crownY - 4f), Offset(centerX, crownY + 12f), size.width * .05f)
                    drawLine(accentColor, Offset(centerX, crownY + 12f), Offset(size.width * .60f, crownY - 4f), size.width * .05f)
                    drawLine(accentColor, Offset(size.width * .60f, crownY - 4f), Offset(size.width * .66f, crownY + 12f), size.width * .05f)
                    drawCircle(accentColor, radius = size.width * .045f, center = Offset(size.width * .18f, size.height * .32f), style = Stroke(size.width * .025f))
                    drawCircle(accentColor, radius = size.width * .035f, center = Offset(size.width * .84f, size.height * .45f), style = Stroke(size.width * .02f))
                }
            }
        }
    }
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
