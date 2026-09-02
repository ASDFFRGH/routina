package com.routina.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.routina.app.app.RoutinaApp
import com.routina.app.ui.theme.RoutinaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (application as RoutinaApplication).container.routineRepository
        setContent {
            RoutinaTheme {
                RoutinaApp(repository = repository)
            }
        }
    }
}
