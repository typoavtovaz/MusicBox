package com.margy.musicbox

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.margy.musicbox.playback.AudioReactive
import com.margy.musicbox.playback.PlayerEngine
import com.margy.musicbox.ui.AppRoot
import com.margy.musicbox.ui.theme.MusicBoxTheme

class MainActivity : ComponentActivity() {

    private val audioPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            AudioReactive.permissionGranted = granted
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        PlayerEngine.init(this)
        askAudioPermission()

        setContent {
            MusicBoxTheme {
                AppRoot()
            }
        }
    }

    private fun askAudioPermission() {
        val ok = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        AudioReactive.permissionGranted = ok
        if (!ok) audioPermission.launch(Manifest.permission.RECORD_AUDIO)
    }
}
