package com.example.syncadapterapplication.syncservice

import android.accounts.Account
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.Context
import android.content.SyncResult
import android.os.Bundle
import android.util.Log
import com.example.syncadapterapplication.ContactsManager

class SyncAdapter(context: Context, autoinit: Boolean) : AbstractThreadedSyncAdapter(context, autoinit) {

    private val contactsManager = ContactsManager(context.contentResolver)

    companion object {
        const val TAG = "SyncAdapter"
    }

    override fun onPerformSync(account: Account?, extras: Bundle?, authority: String?, provider: ContentProviderClient?,
                               syncResult: SyncResult?) {
        Log.e(TAG, "On perform sync started")
        Log.e(TAG, "Sync performed")
    }
}