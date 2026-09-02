package com.routina.app.feature.routines

import com.routina.app.domain.model.Frequency
import com.routina.app.domain.model.Routine
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutinePresentationTest {
    @Test fun frequencyDisplayName_usesJapanesePresetsAndCustomDays() {
        assertEquals("毎日", Frequency.Daily.displayName())
        assertEquals("3日ごと", Frequency.EveryThreeDays.displayName())
        assertEquals("週1回", Frequency.Weekly.displayName())
        assertEquals("5日ごと", Frequency.EveryDays(5).displayName())
    }

    @Test fun formValidation_rejectsBlankNameAndInvalidCustomInterval() {
        assertFalse(isRoutineFormValid(" ", FrequencyPreset.DAILY, ""))
        assertFalse(isRoutineFormValid("読書", FrequencyPreset.CUSTOM, "0"))
        assertFalse(isRoutineFormValid("読書", FrequencyPreset.CUSTOM, "abc"))
        assertTrue(isRoutineFormValid("読書", FrequencyPreset.CUSTOM, "4"))
    }

    @Test fun formViewModel_exposesValidationThroughState() {
        val viewModel = RoutineFormViewModel(FakeRoutineRepository())
        viewModel.updateName("散歩")
        viewModel.updatePreset(FrequencyPreset.CUSTOM)
        viewModel.updateCustomInterval("2")
        assertTrue(viewModel.state.value.isValid)
        viewModel.updateCustomInterval("0")
        assertFalse(viewModel.state.value.isValid)
    }

    @Test fun listViewModel_archivesActiveRoutineFromTomorrow() = runBlocking {
        val today = LocalDate.of(2026, 9, 3)
        val repository = FakeRoutineRepository()
        val viewModel = RoutineListViewModel(repository) { today }
        val routine = Routine("routine-1", "読書", today, Frequency.Daily, 20, 10, 0)

        viewModel.archiveIfActive(routine)

        assertEquals("routine-1", repository.archivedRoutineId)
        assertEquals(today.plusDays(1), repository.archivedOn)
    }

    @Test fun listViewModel_doesNotArchiveInactiveRoutine() = runBlocking {
        val repository = FakeRoutineRepository()
        val viewModel = RoutineListViewModel(repository)
        val routine = Routine("routine-1", "読書", LocalDate.of(2026, 9, 3), Frequency.Daily, 20, 10, 0, 1)

        viewModel.archiveIfActive(routine)

        assertEquals(null, repository.archivedRoutineId)
        assertEquals(null, repository.archivedOn)
    }
}
