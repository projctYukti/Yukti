package com.example.learningcompose.update

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.widget.Toast
import com.google.firebase.database.*

object UpdateChecker {

    fun checkForUpdates(context: Context, onUpdateAvailable: (String) -> Unit) {
        // Get the current version code
        val currentVersionCode = getAppVersionCode(context)

        // Firebase Realtime Database reference for update info
        val updateInfoRef = FirebaseDatabase.getInstance().getReference("update_info")

        // Listen to the update info once
        updateInfoRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                // Get the latest version and APK URL from Firebase
                val latestVersion = snapshot.child("latest_version").getValue(Int::class.java)
                val apkUrl = snapshot.child("apk_url").getValue(String::class.java)

                // Check if an update is available
                if (latestVersion != null && latestVersion > currentVersionCode && apkUrl != null) {
                    Toast.makeText(context, "New update available", Toast.LENGTH_SHORT).show()

                    // Trigger the callback with the APK URL
                    onUpdateAvailable(apkUrl)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors from Firebase
                Toast.makeText(context, "Failed to check for updates", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getAppVersionCode(context: Context): Int {
        return try {
            val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            -1 // Return -1 if the version code is not found
        }
    }
}
