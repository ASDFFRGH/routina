@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.routina.app.feature.routines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.routina.app.domain.model.Routine
import com.routina.app.domain.repository.RoutineRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun RoutineListRoute(
    repository: RoutineRepository,
    onAddRoutine: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RoutineListViewModel = viewModel(factory = RoutineListViewModelFactory(repository)),
) {
    val routines by viewModel.routines.collectAsStateWithLifecycle()
    RoutineListScreen(routines, onAddRoutine, viewModel::archive, modifier)
}

@Composable
fun RoutineListScreen(
    routines: List<Routine>,
    onAddRoutine: () -> Unit,
    onArchive: (Routine) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ordered = remember(routines) { routines.sortedWith(compareBy<Routine> { !it.isActive() }.thenBy { it.name }) }
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("ルーティーン") }) },
        floatingActionButton = { FloatingActionButton(onClick = onAddRoutine) { Text("追加") } },
    ) { padding ->
        if (ordered.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text("ルーティーンはまだありません", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("追加して、毎日の習慣を育てましょう。")
                Spacer(Modifier.height(16.dp))
                Button(onClick = onAddRoutine) { Text("ルーティーンを追加") }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(ordered, key = Routine::id) { routine ->
                    RoutineRow(routine, onArchive)
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun RoutineRow(routine: Routine, onArchive: (Routine) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(routine.name, style = MaterialTheme.typography.titleMedium)
            Text("${routine.frequency.displayName()} ・ 開始 ${routine.startDate}")
            Text("報酬: ${routine.rewardXp} XP / ${routine.rewardPoints} ポイント")
            if (!routine.isActive()) Text("アーカイブ済み")
        }
        if (routine.isActive()) {
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = { onArchive(routine) }) { Text("アーカイブ") }
        }
    }
}

@Composable
fun RoutineFormRoute(
    repository: RoutineRepository,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RoutineFormViewModel = viewModel(factory = RoutineFormViewModelFactory(repository)),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.saved.collect { onSaved() } }
    RoutineFormScreen(state, viewModel::updateName, viewModel::updateStartDate, viewModel::updatePreset, viewModel::updateCustomInterval, viewModel::save, onCancel, modifier)
}

@Composable
fun RoutineFormScreen(
    state: RoutineFormState,
    onNameChanged: (String) -> Unit,
    onStartDateChanged: (LocalDate) -> Unit,
    onPresetChanged: (FrequencyPreset) -> Unit,
    onCustomIntervalChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    Scaffold(modifier = modifier, topBar = { TopAppBar(title = { Text("ルーティーンを登録") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(state.name, onNameChanged, Modifier.fillMaxWidth(), label = { Text("名前") }, singleLine = true, isError = state.name.isNotEmpty() && state.name.isBlank())
            OutlinedTextField(state.startDate.toString(), {}, Modifier.fillMaxWidth(), label = { Text("開始日") }, readOnly = true, trailingIcon = { TextButton(onClick = { showDatePicker = true }) { Text("変更") } })
            FrequencySelector(state.preset, onPresetChanged)
            if (state.preset == FrequencyPreset.CUSTOM) {
                OutlinedTextField(state.customInterval, onCustomIntervalChanged, Modifier.fillMaxWidth(), label = { Text("何日ごと") }, suffix = { Text("日") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), isError = state.customInterval.isNotEmpty() && state.customInterval.toLongOrNull()?.let { it < 1 } != false)
            }
            Text("完了すると 20 XP と 10 ポイントを獲得します。")
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onCancel, enabled = !state.isSaving) { Text("キャンセル") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onSave, enabled = state.isValid && !state.isSaving) { Text(if (state.isSaving) "保存中…" else "保存") }
            }
        }
    }
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.startDate.toEpochDay() * 86_400_000L)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { onStartDateChanged(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) }; showDatePicker = false }) { Text("決定") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("キャンセル") } },
        ) { DatePicker(datePickerState) }
    }
}

@Composable
private fun FrequencySelector(selected: FrequencyPreset, onSelected: (FrequencyPreset) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.label(), onValueChange = {}, readOnly = true, label = { Text("頻度") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            FrequencyPreset.entries.forEach { preset ->
                DropdownMenuItem(text = { Text(preset.label()) }, onClick = { onSelected(preset); expanded = false })
            }
        }
    }
}

private fun FrequencyPreset.label(): String = when (this) {
    FrequencyPreset.DAILY -> "毎日"
    FrequencyPreset.EVERY_THREE_DAYS -> "3日ごと"
    FrequencyPreset.WEEKLY -> "週1回"
    FrequencyPreset.CUSTOM -> "任意日数"
}
