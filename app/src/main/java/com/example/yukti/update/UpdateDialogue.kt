package com.example.learningcompose.update

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ShowUpdateDialog(
    onDismiss: () -> Unit,  // Lambda function to dismiss the dialog
    onUpdate: () -> Unit    // Lambda function to handle the update button press
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Update Available") },
        text = { Text("A new version of the app is available. Do you want to update?") },
        confirmButton = {
            Button(onClick = {
                onUpdate()
                onDismiss()
            }) {
                Text("Update")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}
