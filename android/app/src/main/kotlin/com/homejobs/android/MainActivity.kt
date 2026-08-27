package com.homejobs.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.homejobs.android.ui.navigation.HomeJobsNavGraph
import com.homejobs.android.ui.theme.HomeJobsTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HomeJobsTrackerTheme {
                HomeJobsNavGraph()
            }
        }
    }
}
