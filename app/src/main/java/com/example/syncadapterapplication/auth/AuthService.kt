package com.example.syncadapterapplication.auth

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class AuthService : Service() {

    private lateinit var authenticator : Authenticator

    companion object {
        const val TAG = "AuthService"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Start auth service")
        authenticator = Authenticator(this)
    }

    override fun onBind(intent: Intent?): IBinder? = authenticator.iBinder
}