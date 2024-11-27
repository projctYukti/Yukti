package com.example.yukti.createbusiness

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.yukti.navigation.Routes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun BusinessSetupPage(navController: NavHostController, userId: String) {
    val context = LocalContext.current
    val database = FirebaseDatabase.getInstance()
    val businessesRef = database.getReference("businesses")

    var businessName by remember { mutableStateOf("") }
    var numberOfPeople by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(25.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Set Up Your Business",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TextField(
            value = businessName,
            onValueChange = { businessName = it },
            label = { Text("Business Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = numberOfPeople,
            onValueChange = { numberOfPeople = it },
            label = { Text("Number of People") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                try {
                    if (businessName.isNotBlank() && numberOfPeople.isNotBlank()) {
                        val number = numberOfPeople.toIntOrNull()
                        if (number == null || number <= 0) {
                            Toast.makeText(context, "Invalid number of people", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isLoading = true

                        generateUniqueCode(businessesRef) { generatedCode ->
                            val businessId = businessesRef.push().key ?: return@generateUniqueCode
                            val businessData = mapOf(
                                "businessName" to businessName,
                                "numberOfPeople" to number,
                                "adminId" to userId,
                                "uniqueCode" to generatedCode,
                                "members" to mapOf(userId to true) // Nested map for members
                            )


                            // Save to Firebase
                            businessesRef.child(businessId).setValue(businessData)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        context,
                                        "Business setup successful!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    navController.navigate(Routes.chat)
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        context,
                                        "Error: ${it.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                .addOnCompleteListener { isLoading = false }
                        }
                    } else {
                        Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Saving..." else "Save and Continue")
        }
    }
}

fun generateUniqueCode(businessesRef: DatabaseReference, callback: (String) -> Unit) {
    val generatedCode = (1..6)
        .map { ('A'..'Z') + ('0'..'9') }
        .flatten()
        .shuffled()
        .take(6)
        .joinToString("")

    // Check uniqueness
    businessesRef.orderByChild("uniqueCode").equalTo(generatedCode)
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    generateUniqueCode(businessesRef, callback) // Retry if not unique
                } else {
                    callback(generatedCode) // Use the code if unique
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
}
