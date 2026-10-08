package com.ngao.maternalcare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ngao.maternalcare.ui.nav.NgaoNavGraph
import com.ngao.maternalcare.ui.theme.NgaoMaternalCareTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as NgaoApp

        setContent {
            NgaoMaternalCareTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NgaoNavGraph(
                        repository = app.repository,
                        sessionManager = app.sessionManager
                    )
                }
            }
        }
    }
}
