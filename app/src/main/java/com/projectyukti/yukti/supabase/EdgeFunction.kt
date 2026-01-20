package com.projectyukti.yukti.supabase

import android.util.Log
import okhttp3.Call
import okhttp3.Callback

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class EdgeFunction {
    fun sendPushViaEdgeFunction(
        playerId: String,
        title: String,
        message: String
    ) {
        val client = OkHttpClient()

        val json = """
        {
          "playerId": "$playerId",
          "title": "$title",
          "message": "$message"
        }
    """

        val body = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://afhyzsttnxygfbuhdzjx.supabase.co/functions/v1/clever-responder")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("EdgeFunction", "Failed to send notification", e)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("EdgeFunction", "Response: ${response.body?.string()}")
            }
        })
    }

}