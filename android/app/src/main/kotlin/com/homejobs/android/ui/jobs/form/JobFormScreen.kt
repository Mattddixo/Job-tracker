package com.homejobs.android.ui.jobs.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homejobs.android.domain.model.JobStatus
import com.homejobs.android.domain.model.JobUpsertInput
import com.homejobs.android.domain.model.PaymentStatus
import com.homejobs.android.ui.common.EnumDropdown
import com.homejobs.android.ui.common.LoadingState
import com.homejobs.android.ui.common.fireLinkedCallback
import com.homejobs.android.ui.common.toDisplayDate
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobFormScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: JobFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditing) "Edit job" else "New job") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            LoadingState(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        val input = uiState.input
        fun update(transform: (JobUpsertInput) -> JobUpsertInput) = viewModel.updateInput(transform)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FormSection(title = "Job Details") {
                OutlinedTextField(
                    value = input.title,
                    onValueChange = { text -> update { it.copy(title = text) } },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.errors.any { it.contains("Title") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
                OutlinedTextField(
                    value = input.category.orEmpty(),
                    onValueChange = { text -> update { it.copy(category = text.ifBlank { null }) } },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
                OutlinedTextField(
                    value = input.location.orEmpty(),
                    onValueChange = { text -> update { it.copy(location = text.ifBlank { null }) } },
                    label = { Text("Room / location") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
            }

            FormSection(title = "Vendor") {
                OutlinedTextField(
                    value = input.vendorName.orEmpty(),
                    onValueChange = { text -> update { it.copy(vendorName = text.ifBlank { null }) } },
                    label = { Text("Vendor / contractor") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
                OutlinedTextField(
                    value = input.vendorContact.orEmpty(),
                    onValueChange = { text -> update { it.copy(vendorContact = text.ifBlank { null }) } },
                    label = { Text("Vendor contact") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
            }

            FormSection(title = "Status & Schedule") {
                EnumDropdown(
                    label = "Status",
                    options = JobStatus.entries,
                    selected = input.status,
                    optionLabel = { it.name.replace('_', ' ') },
                    onSelected = { status -> update { it.copy(status = status) } },
                )
                DateField(
                    label = "Scheduled date",
                    value = input.scheduledDate,
                    onValueChange = { date -> update { it.copy(scheduledDate = date) } },
                )
                DateField(
                    label = "Completed date",
                    value = input.completedDate,
                    onValueChange = { date -> update { it.copy(completedDate = date) } },
                )
                DateField(
                    label = "Warranty expiry",
                    value = input.warrantyExpiry,
                    onValueChange = { date -> update { it.copy(warrantyExpiry = date) } },
                )
            }

            FormSection(title = "Cost Tracking") {
                NumberField(
                    label = "Quoted cost ($)",
                    initialValue = input.quotedCost,
                    onValueChange = { value -> update { it.copy(quotedCost = value) } },
                )
                NumberField(
                    label = "Actual cost ($)",
                    initialValue = input.actualCost,
                    onValueChange = { value -> update { it.copy(actualCost = value) } },
                )
                val costVariance = variance(input.quotedCost, input.actualCost)
                VarianceLine(label = "Cost variance", variance = costVariance, format = ::formatCostVariance)
            }

            FormSection(title = "Time Tracking") {
                NumberField(
                    label = "Predicted hours",
                    initialValue = input.predictedHours,
                    onValueChange = { value -> update { it.copy(predictedHours = value) } },
                )
                NumberField(
                    label = "Actual hours",
                    initialValue = input.actualHours,
                    onValueChange = { value -> update { it.copy(actualHours = value) } },
                )
                val timeVariance = variance(input.predictedHours, input.actualHours)
                VarianceLine(label = "Time variance", variance = timeVariance, format = ::formatHoursVariance)
            }

            FormSection(title = "Payment") {
                EnumDropdown(
                    label = "Payment status",
                    options = PaymentStatus.entries,
                    selected = input.paymentStatus,
                    optionLabel = { it.name },
                    onSelected = { status -> update { it.copy(paymentStatus = status) } },
                )
                PaymentMethodField(
                    methods = uiState.paymentMethods,
                    selectedId = input.paymentMethodId,
                    onSelected = { id -> update { it.copy(paymentMethodId = id) } },
                    onAddNew = { name, maxCredit -> viewModel.createPaymentMethod(name, maxCredit) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (uiState.errors.isNotEmpty()) {
                Column {
                    uiState.errors.forEach { error ->
                        Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            uiState.saveError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    viewModel.save { job, isNewlyCreated ->
                        // Only on the create that just established a link — never on a later
                        // edit-save of an already-linked job, which would otherwise re-fire this
                        // and bounce Job Jar to the foreground on every unrelated edit.
                        if (isNewlyCreated) {
                            job.linkedJobJarId?.let { jobJarId ->
                                fireLinkedCallback(context, jobJarId = jobJarId, trackerJobId = job.id)
                            }
                        }
                        onSaved()
                    }
                },
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (uiState.isSaving) "Saving…" else "Save")
            }
        }
    }
}

/** actual − quoted/predicted, the same convention as Job.costVariance/timeVariance. */
private fun variance(planned: Double?, actual: Double?): Double? =
    if (planned != null && actual != null) actual - planned else null

private fun formatCostVariance(variance: Double): String =
    (if (variance >= 0) "+$" else "-$") + "%.2f".format(abs(variance))

private fun formatHoursVariance(variance: Double): String =
    "%+.1f hrs".format(variance)

@Composable
private fun FormSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun VarianceLine(label: String, variance: Double?, format: (Double) -> String) {
    if (variance == null) return
    val color = when {
        variance > 0 -> MaterialTheme.colorScheme.tertiary
        variance < 0 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        text = "$label: ${format(variance)}",
        color = color,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
    )
}

/**
 * Keeps its own text buffer rather than deriving the displayed text from [initialValue] on every
 * recomposition: re-deriving from the parsed Double (e.g. `1.0.toString()`) would rewrite "1" to
 * "1.0" mid-keystroke and make it impossible to type something like "12.5". The buffer is seeded
 * once, when the field first enters composition (which — because the form shows a loading state
 * until any existing job data has arrived — already has the right value in both create and edit
 * mode).
 */
@Composable
private fun NumberField(
    label: String,
    initialValue: Double?,
    onValueChange: (Double?) -> Unit,
) {
    var text by remember { mutableStateOf(initialValue?.toInputString().orEmpty()) }
    OutlinedTextField(
        value = text,
        onValueChange = { newText ->
            if (newText.matches(Regex("^\\d*\\.?\\d*$"))) {
                text = newText
                onValueChange(newText.toDoubleOrNull())
            }
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun Double.toInputString(): String =
    if (this == Math.floor(this) && !this.isInfinite()) toLong().toString() else toString()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    label: String,
    value: String?,
    onValueChange: (String?) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }

    // A readOnly OutlinedTextField still consumes taps internally for focus/cursor handling, so a
    // `.clickable` attached directly to it never fires reliably. An invisible clickable Box
    // stacked on top intercepts the tap first instead.
    Box {
        OutlinedTextField(
            value = value?.toDisplayDate().orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text("Not set") },
            trailingIcon = { Icon(Icons.Filled.DateRange, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { showPicker = true },
        )
    }

    if (showPicker) {
        val initialMillis = value?.let {
            LocalDate.parse(it).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        // DatePicker works in UTC millis; encoding/decoding with ZoneOffset.UTC on both ends
        // avoids the classic off-by-one-day bug from mixing in the device's local zone.
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                Row {
                    if (value != null) {
                        TextButton(onClick = {
                            onValueChange(null)
                            showPicker = false
                        }) { Text("Clear") }
                    }
                    TextButton(onClick = {
                        val millis = datePickerState.selectedDateMillis
                        onValueChange(
                            millis?.let {
                                Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString()
                            },
                        )
                        showPicker = false
                    }) { Text("OK") }
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
