package com.example.bd_system

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.FirebaseDatabase

class BDSystemApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val options = FirebaseOptions.Builder()
            .setApplicationId("1:775623513742:android:7ab4a637169687eb02dbdf")
            .setApiKey("AIzaSyA9c4vcZXouw9VH7mpK-lUYSB4AJ8BXdBA")
            .setProjectId("bd-system-parshva")
            .setGcmSenderId("775623513742")
            .setStorageBucket("bd-system-parshva.firebasestorage.app")
            .setDatabaseUrl("https://bd-system-parshva-default-rtdb.asia-southeast1.firebasedatabase.app/") 
            .build()

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this, options)
        }
        
        // Enable disk persistence to handle slow/flaky connections better.
        // This allows the app to cache data locally.
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (e: Exception) {
            // Persistence must be set before any other usage of the database
        }
    }
}
