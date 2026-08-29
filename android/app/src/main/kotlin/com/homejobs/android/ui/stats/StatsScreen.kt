package com.homejobs.android.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homejobs.android.ui.common.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stats") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        StatsContent(uiState = uiState, modifier = Modifier.padding(padding))
    }
}

@Composable
private fun StatsContent(uiState: StatsUiState, modifier: Modifier = Modifier) {
    if (uiState.methodStats.isEmpty()) {
        EmptyState("No cost data yet.", modifier = modifier.fillMaxSize())
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { SummaryCard(uiState.totalPaid, uiState.totalOwed) }
        items(uiState.methodStats, key = { it.method?.id ?: -1L }) { stat ->
            MethodStatCard(stat)
        }
    }
}

@Composable
private fun SummaryCard(totalPaid: Double, totalOwed: Double) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("All jobs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            StatRow("Paid", "$%.2f".format(totalPaid), MaterialTheme.colorScheme.secondary)
            StatRow("Owed (partial + unpaid)", "$%.2f".format(totalOwed), MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
private fun MethodStatCard(stat: MethodStat) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stat.method?.name ?: "Unassigned", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "${stat.jobCount} job${if (stat.jobCount == 1) "" else "s"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StatRow("Paid", "$%.2f".format(stat.paidTotal), MaterialTheme.colorScheme.secondary)
            if (stat.partialTotal > 0) {
                StatRow("Partial", "$%.2f".format(stat.partialTotal), MaterialTheme.colorScheme.tertiary)
            }
            StatRow("Unpaid", "$%.2f".format(stat.unpaidTotal), MaterialTheme.colorScheme.tertiary)

            val maxCredit = stat.method?.maxCredit
            if (maxCredit != null && maxCredit > 0) {
                val used = stat.partialTotal + stat.unpaidTotal
                val fraction = (used / maxCredit).toFloat().coerceIn(0f, 1f)
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(progress = fraction, modifier = Modifier.fillMaxWidth())
                Text(
                    "$%.2f of $%.2f limit used".format(used, maxCredit),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = color)
    }
}
