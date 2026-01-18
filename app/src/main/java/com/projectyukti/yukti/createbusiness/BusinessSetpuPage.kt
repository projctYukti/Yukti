package com.projectyukti.yukti.createbusiness

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.projectyukti.yukti.navigation.Routes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@OptIn(ExperimentalMaterial3Api::class)
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
            .padding(start = 15.dp, end = 15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(title = {
            Text("Create a Business",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
            navigationIcon = {
                IconButton(onClick = {
                    navController.navigate(Routes.chat){
                        popUpTo(navController.graph.startDestinationId)
                        {
                            inclusive = true
                        }
                    }

                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },)
        OutlinedTextField(

            value = businessName,
            onValueChange = { businessName = it },

            label = { Text("Business Name") },

            shape = RoundedCornerShape(20.dp) // Border radius

        )


        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(

            value = numberOfPeople,
            onValueChange = { numberOfPeople = it },

            label = { Text("Number of people") },

            shape = RoundedCornerShape(20.dp) // Border radius

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

                        // Generate a unique code for the business
                        generateUniqueCode(businessesRef) { generatedCode ->
                            val businessId = businessesRef.push().key ?: return@generateUniqueCode
                            val businessData = mapOf(
                                "businessName" to businessName,
                                "numberOfPeople" to number,
                                "adminId" to userId,
                                "uniqueCode" to generatedCode,
                                "members" to mapOf(userId to true) // Nested map for members
                            )

                            // Save business data to Firebase
                            businessesRef.child(businessId).setValue(businessData)
                                .addOnSuccessListener {
                                    // Update user subscription details in the "users" node
                                    val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
                                    val userData = mapOf(
                                        "isSubscribed" to true, // Set subscription status to true
                                        "businessId" to mapOf(businessId to true) // Associate the user with the business

                                    )
                                    userRef.updateChildren(userData)
                                        .addOnSuccessListener {
                                            // After updating user data, show success message and navigate
                                            Toast.makeText(context, "Business setup successful and subscription updated!", Toast.LENGTH_LONG).show()
                                            navController.navigate(Routes.chat)
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "Failed to update user subscription: ${it.message}", Toast.LENGTH_LONG).show()
                                        }

                                    // Optionally, you can also navigate to another screen
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
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
            modifier = Modifier.padding(start = 20.dp,end = 20.dp),
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
