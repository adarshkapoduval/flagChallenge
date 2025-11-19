package com.example.mvvm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mvvm.vm.ChallengeViewModel

@Composable
fun ScheduleScreen(viewModel: ChallengeViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
        Text("Schedule challenge (demo uses now + 10s)")
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { viewModel.scheduleChallenge(System.currentTimeMillis() + 10_000L) }) {
            Text("Schedule to start in 10s (demo)")
        }
    }
}