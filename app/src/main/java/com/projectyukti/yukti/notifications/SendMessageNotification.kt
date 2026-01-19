package com.projectyukti.yukti.notifications
import android.util.Log
import okhttp3.*
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException


class SendMessageNotification {

    fun sendNotificationToReceiver(receiverFcmToken: String, currentUserName: String, message: String) {
        // Use a Coroutine to ensure the network request runs in a background thread
        CoroutineScope(Dispatchers.IO).launch {
            val client = OkHttpClient()

            // JSON payload for OneSignal
            val json = """
            {
                "app_id": "02eb57f2-fe80-41d7-917c-ed6cdb1b6567",
                "include_player_ids": ["$receiverFcmToken"],
                "contents": {"en": "$message"},
                "headings": {"en": "$currentUserName"},
                "data": {"key1": "value1", "key2": "value2"}
            }
            """

            val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json)

            val request = Request.Builder()
                .url("https://onesignal.com/api/v1/notifications")
                .post(body)
                .addHeader(
                    "Authorization",
                    "os_v2_app_alvvp4x6qba5pel45vwnwg3fm5266wuwblkegc54gmf2eofrjwlv2t5mrxi436tu6r72kr3zfu3znk42suabuzrtmc3zoc2go3cudxq"
                )
                .build()

            try {
                val response = client.newCall(request).execute()

                // Log the response details
                if (response.isSuccessful) {
                    Log.d("OneSignal", "Notification sent successfully")
                } else {
                    val errorBody = response.body?.string()
                    Log.e("OneSignal", "Error sending notification: ${response.message} (Code: ${response.code})")
                    Log.e("OneSignal", "Error body: $errorBody")
                }
            } catch (e: Exception) {
                Log.e("OneSignal", "Exception: ${e.message}", e)
            }
        }
    }
}
