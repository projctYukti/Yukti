package com.example.yukti.createbusiness.joinbusiness

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.yukti.navigation.Routes
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


@OptIn(ExperimentalMaterial3Api::class)
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
            .padding(start = 15.dp, end = 15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(title = {
            Text("Join a Business",
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

            value = uniqueCode,
            onValueChange = { uniqueCode = it },
            label = { Text("Enter Business Code") },
            shape = RoundedCornerShape(20.dp) // Border radius

        )


        Spacer(modifier = Modifier.height(32.dp))

        Button(
            modifier = Modifier.padding(start = 20.dp,end = 20.dp),
            onClick = {
                if (uniqueCode.isNotBlank()) {
                    isLoading = true

                    val usersRef = FirebaseDatabase.getInstance().getReference("users")

                    // Search for the business by unique code
                    businessesRef.orderByChild("uniqueCode").equalTo(uniqueCode)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    val business = snapshot.children.firstOrNull()
                                    val businessId = business?.key

                                    // Log businessId for debugging
                                    println("Business ID: $businessId")

                                    val maxMembers = business?.child("numberOfPeople")?.getValue(Int::class.java) ?: 0
                                    val currentMembers = business?.child("members")?.childrenCount?.toInt() ?: 0

                                    // Check if `businessId` and `userId` are valid
                                    if (businessId != null && userId.isNotEmpty()) {
                                        val membersRef = businessesRef.child(businessId).child("members")

                                        // Check if the user is already a member
                                        membersRef.child(userId).get().addOnSuccessListener { memberSnapshot ->
                                            if (memberSnapshot.exists()) {
                                                // User is already a member
                                                Toast.makeText(
                                                    context,
                                                    "You are already a member of this business.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                isLoading = false
                                            } else {
                                                // Check if the business is full
                                                if (currentMembers < maxMembers) {
                                                    // Add user as a member
                                                    membersRef.child(userId).setValue(true)
                                                        .addOnSuccessListener {
                                                            // Add the businessId to the user's `businesses` node
                                                            usersRef.child(userId).apply {
                                                                // Update businesses node
                                                                child("businessId").child(businessId).setValue(true)
                                                                    .addOnSuccessListener {
                                                                        // Update isSubscribed field
                                                                        child("isSubscribed").setValue(false)
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
                                                                                    "Failed to update subscription status: ${it.message}",
                                                                                    Toast.LENGTH_LONG
                                                                                ).show()
                                                                            }
                                                                    }
                                                                    .addOnFailureListener {
                                                                        Toast.makeText(
                                                                            context,
                                                                            "Failed to update user node: ${it.message}",
                                                                            Toast.LENGTH_LONG
                                                                        ).show()
                                                                    }
                                                                    .addOnCompleteListener { isLoading = false }
                                                            }

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
                                                        "The business is already full.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    isLoading = false
                                                }
                                            }
                                        }.addOnFailureListener {
                                            // Handle potential errors when reading the members node
                                            Toast.makeText(
                                                context,
                                                "Failed to check membership: ${it.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            isLoading = false
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Invalid business ID or user ID.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        isLoading = false
                                    }
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

            enabled = !isLoading
        ) {
            Text(if (isLoading) "Joining..." else "Join Business")
        }




    }
}
