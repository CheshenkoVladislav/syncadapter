package com.example.syncadapterapplication.syncservice

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class SyncService : Service() {

    private var syncAdapter: SyncAdapter? = null
    private val lock = Any()

    companion object {
        const val TAG = "SyncService"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Start sync service")
        synchronized(lock) {
            if (syncAdapter == null) {
                Log.d(TAG, "Start service")
                syncAdapter = SyncAdapter(applicationContext, true)
            }
        }
        Log.d(TAG, "Sync adapter created")
    }

    override fun onBind(intent: Intent?): IBinder? = syncAdapter?.syncAdapterBinder
}