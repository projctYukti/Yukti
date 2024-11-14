package com.example.yukti

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.learningcompose.update.ShowUpdateDialog
import com.example.learningcompose.update.UpdateChecker
import com.example.yukti.ui.theme.YuktiTheme

class MainActivity : ComponentActivity() {

    private var showDialog by mutableStateOf(false)
    private var apkUrl: String? by mutableStateOf(null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkForUpdates()
        enableEdgeToEdge()
        setContent {
            YuktiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )

                    // Show the update dialog if needed
                    if (showDialog && apkUrl != null) {
                        ShowUpdateDialog(
                            onDismiss = { showDialog = false },
                            onUpdate = { apkUrl?.let { downloadAndInstallApk(it) } }
                        )
                    }
                }
            }

        }
    }
    private fun checkForUpdates() {
        val context = this

        UpdateChecker.checkForUpdates(context) { url ->
            apkUrl = url
            showDialog = true
        }
    }
    private fun downloadAndInstallApk(apkUrl: String) {
        // Launch the browser to open the APK download URL
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl))
        startActivity(intent)
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

