package com.homejobs.android.ui.jobs.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homejobs.android.domain.model.JobStatus
import com.homejobs.android.domain.model.PaymentStatus
import com.homejobs.android.ui.common.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobFormScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: JobFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = input.title,
                onValueChange = { text -> viewModel.updateInput { it.copy(title = text) } },
                label = { Text("Title *") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.errors.any { it.contains("Title") },
            )
            OutlinedTextField(
                value = input.category.orEmpty(),
                onValueChange = { text -> viewModel.updateInput { it.copy(category = text.ifBlank { null }) } },
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = input.location.orEmpty(),
                onValueChange = { text -> viewModel.updateInput { it.copy(location = text.ifBlank { null }) } },
                label = { Text("Room / location") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = input.vendorName.orEmpty(),
                onValueChange = { text -> viewModel.updateInput { it.copy(vendorName = text.ifBlank { null }) } },
                label = { Text("Vendor / contractor") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = input.vendorContact.orEmpty(),
                onValueChange = { text -> viewModel.updateInput { it.copy(vendorContact = text.ifBlank { null }) } },
                label = { Text("Vendor contact") },
                modifier = Modifier.fillMaxWidth(),
            )

            EnumDropdown(
                label = "Status",
                options = JobStatus.entries,
                selected = input.status,
                optionLabel = { it.name.replace('_', ' ') },
                onSelected = { status -> viewModel.updateInput { it.copy(status = status) } },
            )

            OutlinedTextField(
                value = input.quotedCost?.toString().orEmpty(),
                onValueChange = { text -> viewModel.updateInput { it.copy(quotedCost = text.toDoubleOrNull()) } },
                label = { Text("Quoted cost") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = input.actualCost?.toString().orEmpty(),
                onValueChange = { text -> viewModel.updateInput { it.copy(actualCost = text.toDoubleOrNull()) } },
                label = { Text("Actual cost") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = input.predictedHours?.toString().orEmpty(),
                onValueChange = { text -> viewModel.updateInput { it.copy(predictedHours = text.toDoubleOrNull()) } },
                label = { Text("Predicted hours") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = input.actualHours?.toString().orEmpty(),
                onValueChange = { text -> viewModel.updateInput { it.copy(actualHours = text.toDoubleOrNull()) } },
                label = { Text("Actual hours") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = input.scheduledDate.orEmpty(),
                onValueChange = { text -> viewModel.updateInput { it.copy(scheduledDate = text.ifBlank { null }) } },
                label = { Text("Scheduled date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = input.completedDate.orEmpty(),
                onValueChange = { text -> viewModel.updateInput { it.copy(completedDate = text.ifBlank { null }) } },
                label = { Text("Completed date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = input.warrantyExpiry.orEmpty(),
                onValueChange = { text -> viewModel.updateInput { it.copy(warrantyExpiry = text.ifBlank { null }) } },
                label = { Text("Warranty expiry (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
            )

            EnumDropdown(
                label = "Payment status",
                options = PaymentStatus.entries,
                selected = input.paymentStatus,
                optionLabel = { it.name },
                onSelected = { status -> viewModel.updateInput { it.copy(paymentStatus = status) } },
            )
            OutlinedTextField(
                value = input.paymentMethod.orEmpty(),
                onValueChange = { text -> viewModel.updateInput { it.copy(paymentMethod = text.ifBlank { null }) } },
                label = { Text("Payment method") },
                modifier = Modifier.fillMaxWidth(),
            )

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
                onClick = { viewModel.save(onSaved) },
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (uiState.isSaving) "Saving…" else "Save")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> EnumDropdown(
    label: String,
    options: List<T>,
    selected: T,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = optionLabel(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
