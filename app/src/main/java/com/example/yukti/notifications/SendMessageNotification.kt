package com.example.yukti.notifications
import android.util.Log
import com.example.yukti.gitignore.Constants
import okhttp3.*
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import java.io.IOException


class SendMessageNotification {

    fun sendNotificationToUser( fcmToken: String, title: String, message: String) {
        Log.d("FCM", "Sending notification to user: $fcmToken $title $message")
        val serverKey = Constants().serverKey.toString()
        Log.d("ServerKey","Server Key: $serverKey")
        val url = "https://fcm.googleapis.com/fcm/send"

        // Create the JSON payload
        val payload = JsonObject().apply {
            addProperty("to", fcmToken)
            add("notification", JsonObject().apply {
                addProperty("title", title)
                addProperty("body", message)
            })
        }

        // Create MediaType using toMediaType() instead of get or parse
        val mediaType = "application/json".toMediaType()

        // Build the request
        val requestBody = RequestBody.create(mediaType, payload.toString())

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "key=$serverKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        // Send the request
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    println("Notification sent successfully!")
                    Log.d("FCM", "Notification sent successfully!")
                } else {
                    try {
                        val errorBody = response.body?.string()
                        Log.e("FCM", "Error body: $errorBody")
                    } catch (e: IOException) {
                        Log.e("FCM", "Failed to read error body: ${e.message}")
                    }
                    println("Error sending notification: ${response.message}")
                    Log.d("FCM", "Error sending notification: ${response.message}")
                }
            }
        })
    }


}