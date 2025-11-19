package com.example.mvvm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import com.example.mvvm.ui.screens.QuestionScreen
import com.example.mvvm.vm.ChallengeViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Hilt will provide the ViewModel
    private val vm: ChallengeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // You can also obtain the ViewModel inside composables via hiltViewModel()
            QuestionScreen(vm)
        }

        // start the background ticker that drives UI state recomputation
        vm.startTicker()
    }

    override fun onDestroy() {
        vm.stopTicker()
        super.onDestroy()
    }
}
