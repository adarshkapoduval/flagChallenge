package com.adarsh.flag

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
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

    private val notificationPermissionLauncher =
        registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { granted ->

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        }

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

    private fun requestNotificationPermission() {
        notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    override fun onDestroy() {
        vm.stopTicker()
        super.onDestroy()
    }
}
