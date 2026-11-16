package com.margy.musicbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.margy.musicbox.ui.AppRoot
import com.margy.musicbox.ui.theme.MusicBoxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MusicBoxTheme {
                AppRoot()
            }
        }
    }
}
