package com.adarsh.flag

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import dagger.hilt.android.AndroidEntryPoint
import com.adarsh.flag.ui.screens.QuestionScreen
import com.adarsh.flag.vm.ChallengeViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Hilt will provide the ViewModel
    private val vm: ChallengeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable fullscreen mode
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

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
