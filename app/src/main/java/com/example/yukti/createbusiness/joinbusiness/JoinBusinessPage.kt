package com.example.yukti.createbusiness.joinbusiness

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.yukti.navigation.Routes
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


@Composable
fun JoinBusinessPage(navController: NavHostController, userId: String) {
    val context = LocalContext.current
    val database = FirebaseDatabase.getInstance()
    val businessesRef = database.getReference("businesses")

    var uniqueCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Join a Business",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TextField(
            value = uniqueCode,
            onValueChange = { uniqueCode = it },
            label = { Text("Enter Business Code") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (uniqueCode.isNotBlank()) {
                    isLoading = true

                    // Search for the business by unique code
                    businessesRef.orderByChild("uniqueCode").equalTo(uniqueCode)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    val businessId = snapshot.children.first().key ?: return

                                    // Add user as a member
                                    val membersRef = businessesRef.child(businessId).child("members")
                                    membersRef.child(userId).setValue(true)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                context,
                                                "Successfully joined the business!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            navController.navigate(Routes.chat)
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                context,
                                                "Failed to join: ${it.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                        .addOnCompleteListener { isLoading = false }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Invalid business code.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    isLoading = false
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(
                                    context,
                                    "Error: ${error.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                isLoading = false
                            }
                        })
                } else {
                    Toast.makeText(context, "Please enter a code", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Joining..." else "Join Business")
        }
    }
}
