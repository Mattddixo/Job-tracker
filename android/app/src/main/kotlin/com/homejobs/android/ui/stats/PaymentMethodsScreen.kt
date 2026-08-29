package com.homejobs.android.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homejobs.android.domain.model.PaymentMethod
import com.homejobs.android.ui.common.EmptyState
import com.homejobs.android.ui.common.PaymentMethodEditorDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsScreen(
    onBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Methods") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        ManagePaymentMethodsContent(
            methodStats = uiState.methodStats,
            onAdd = viewModel::addPaymentMethod,
            onUpdate = viewModel::updatePaymentMethod,
            onDelete = viewModel::deletePaymentMethod,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
private fun ManagePaymentMethodsContent(
    methodStats: List<MethodStat>,
    onAdd: (String, Double?) -> Unit,
    onUpdate: (Long, String, Double?) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    var deletingStat by remember { mutableStateOf<MethodStat?>(null) }

    val configuredStats = methodStats.filter { it.method != null }

    Box(modifier = modifier.fillMaxSize()) {
        if (configuredStats.isEmpty()) {
            EmptyState("No payment methods yet. Tap + to add one.", modifier = Modifier.fillMaxSize())
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(configuredStats, key = { it.method!!.id }) { stat ->
                    val method = stat.method!!
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(method.name, style = MaterialTheme.typography.bodyLarge)
                                method.maxCredit?.let {
                                    Text("Limit: $%.2f".format(it), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            IconButton(onClick = { editingMethod = method }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit ${method.name}")
                            }
                            IconButton(onClick = { deletingStat = stat }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete ${method.name}")
                            }
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add payment method")
        }
    }

    if (showAddDialog) {
        PaymentMethodEditorDialog(
            title = "Add payment method",
            onDismiss = { showAddDialog = false },
            onSave = { name, maxCredit ->
                onAdd(name, maxCredit)
                showAddDialog = false
            },
        )
    }
    editingMethod?.let { method ->
        PaymentMethodEditorDialog(
            title = "Edit payment method",
            initialName = method.name,
            initialMaxCredit = method.maxCredit,
            onDismiss = { editingMethod = null },
            onSave = { name, maxCredit ->
                onUpdate(method.id, name, maxCredit)
                editingMethod = null
            },
        )
    }
    deletingStat?.let { stat ->
        val method = stat.method!!
        AlertDialog(
            onDismissRequest = { deletingStat = null },
            title = { Text("Delete ${method.name}?") },
            text = {
                Text(
                    if (stat.jobCount > 0) {
                        "${stat.jobCount} job${if (stat.jobCount == 1) "" else "s"} using this will show as Unassigned."
                    } else {
                        "This payment method isn't used by any jobs."
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(method.id)
                    deletingStat = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deletingStat = null }) { Text("Cancel") }
            },
        )
    }
}
