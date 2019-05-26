package com.example.syncadapterapplication

import android.Manifest
import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.example.syncadapterapplication.auth.AuthActivity

class MainActivity : AppCompatActivity() {

    companion object {
        const val AUTH_REQUEST_CODE = 12
        const val PERMISSION_REQUEST_CODE = 13
        const val TAG = "MainActivity"
    }

    private val contactsManager = ContactsManager(contentResolver)

    private lateinit var etPhone: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button = findViewById<Button>(R.id.clearAccount)
        etPhone = findViewById(R.id.etPhone)
        button.setOnClickListener { modifyContact() }
        val authIntent = Intent(this, AuthActivity::class.java)
        startActivityForResult(authIntent, AUTH_REQUEST_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e(TAG, "Auth status: $resultCode")
        if (resultCode == Activity.RESULT_OK) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) !=
                PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SYNC_SETTINGS) !=
                PackageManager.PERMISSION_GRANTED
            )
                requestPermissions(
                    arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS,
                        Manifest.permission.WRITE_SYNC_SETTINGS),
                    PERMISSION_REQUEST_CODE
                )
            else {
                var account: Account? = null
                AccountManager.get(this).accounts.forEach { if (it.type == AuthActivity.ACCOUNT_TYPE) account = it }
                account?.let {
                    ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] ==
                PackageManager.PERMISSION_GRANTED && PackageManager.PERMISSION_GRANTED == grantResults[2]
            ) {
                var account: Account? = null
                AccountManager.get(this).accounts.forEach { if (it.type == AuthActivity.ACCOUNT_TYPE) account = it }
                account?.let {
                    ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true)
                }
            }
        }
    }

    private fun modifyContact() {
        val accountManager = AccountManager.get(this)
        Log.d(TAG, "Add contact")
        var account: Account? = null
        accountManager.accounts.forEach { if (it.type == AuthActivity.ACCOUNT_TYPE) account = it }
        account?.let {
            contactsManager.addAccountFieldToContact(it, etPhone.text.toString())
        }
    }
}