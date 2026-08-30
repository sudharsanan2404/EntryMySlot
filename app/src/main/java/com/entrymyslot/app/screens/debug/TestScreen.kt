package com.entrymyslot.app.screens.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestScreen(
    onBackClick: () -> Unit = {}
) {
    val viewModel: TestViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backend Mission Control") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearLogs() }) {
                        Icon(Icons.Outlined.DeleteSweep, contentDescription = "Clear Logs")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1C1E),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF111318)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // ----------------------------------------------------
            // TEST BUTTONS
            // ----------------------------------------------------
            Text("Available Tests", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TestButton("Get Me", uiState.isRunning) { viewModel.testGetMe() }
                TestButton("List Events", uiState.isRunning) { viewModel.testFetchEvents() }
                TestButton("List Movies", uiState.isRunning) { viewModel.testFetchMovies() }
                TestButton("List Turfs", uiState.isRunning) { viewModel.testFetchTurfs() }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ----------------------------------------------------
            // LOGS DISPLAY
            // ----------------------------------------------------
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Live Logs", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (uiState.isRunning) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.Cyan)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black)
                    .border(1.dp, Color(0xFF2C2E33), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.logs) { log ->
                        LogItem(log)
                    }
                    if (uiState.logs.isEmpty()) {
                        item {
                            Text(
                                "No activity yet. Click a test button above.",
                                color = Color(0xFF45474D),
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TestButton(label: String, isRunning: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isRunning,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2C2E33),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Text(label, fontSize = 12.sp)
    }
}

@Composable
private fun LogItem(log: TestLog) {
    val color = when (log.type) {
        LogType.SUCCESS -> Color(0xFF81C784)
        LogType.ERROR -> Color(0xFFE57373)
        LogType.INFO -> Color(0xFF64B5F6)
    }

    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = "[${log.timestamp}] ",
            color = Color.Gray,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = log.message,
            color = color,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement
    ) {
        content()
    }
}
