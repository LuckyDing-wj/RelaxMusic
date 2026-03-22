package com.relaxmusic.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.relaxmusic.app.ui.RelaxMusicApp
import com.relaxmusic.app.ui.theme.RelaxMusicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RelaxMusicTheme {
                RelaxMusicApp()
            }
        }
    }
}
