package com.routina.app.feature.routines

import com.routina.app.domain.model.Frequency
import com.routina.app.domain.model.Routine
import java.time.LocalDate

enum class FrequencyPreset {
    DAILY,
    EVERY_THREE_DAYS,
    WEEKLY,
    CUSTOM,
}

fun Frequency.displayName(): String = when (intervalDays) {
    1L -> "毎日"
    3L -> "3日ごと"
    7L -> "週1回"
    else -> "${intervalDays}日ごと"
}

fun frequencyFor(preset: FrequencyPreset, customInterval: String): Frequency? = when (preset) {
    FrequencyPreset.DAILY -> Frequency.Daily
    FrequencyPreset.EVERY_THREE_DAYS -> Frequency.EveryThreeDays
    FrequencyPreset.WEEKLY -> Frequency.Weekly
    FrequencyPreset.CUSTOM -> customInterval.toLongOrNull()
        ?.takeIf { it >= 1 }
        ?.let(Frequency::EveryDays)
}

fun isRoutineFormValid(name: String, preset: FrequencyPreset, customInterval: String): Boolean =
    name.isNotBlank() && frequencyFor(preset, customInterval) != null

data class RoutineFormState(
    val name: String = "",
    val startDate: LocalDate = LocalDate.now(),
    val preset: FrequencyPreset = FrequencyPreset.DAILY,
    val customInterval: String = "",
    val isSaving: Boolean = false,
) {
    val isValid: Boolean
        get() = isRoutineFormValid(name, preset, customInterval)
}

fun Routine.isActive(): Boolean = archivedEpochDay == null
