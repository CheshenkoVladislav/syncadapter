package com.example.syncadapterapplication.auth

import android.accounts.AccountAuthenticatorActivity
import android.accounts.AccountManager
import android.os.Bundle
import android.accounts.Account
import android.app.Activity
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.content.ContentResolver
import android.provider.ContactsContract
import com.example.syncadapterapplication.R


class AuthActivity : AccountAuthenticatorActivity() {

    lateinit var etLogin: EditText
    lateinit var etPassword: EditText
    lateinit var btnLogin: Button
    lateinit var am: AccountManager

    companion object {
        const val EXTRA_TOKEN_TYPE = "extra token type"
        const val ACCOUNT_ALREADY_EXIST = "account_already_exists"
        const val ACCOUNT_TYPE = "com.example.syncadapterapplication"
        const val TAG = "AuthActivity"
    }

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        am = AccountManager.get(this)
        val accounts = am.accounts
        Log.d(TAG, "Accounts: $accounts")
        if (accounts.isEmpty() || !accounts.any { it.type == ACCOUNT_TYPE }) {
            setContentView(R.layout.activity_auth)
            etLogin = findViewById(R.id.login)
            etPassword = findViewById(R.id.pass)
            btnLogin = findViewById(R.id.loginBtn)
            btnLogin.setOnClickListener {
                val login = etLogin.text.toString()
                val pass = etPassword.text.toString()
                val account = Account(login, ACCOUNT_TYPE)
                onTokenReceived(account, pass, Authenticator.AUTH_TOKEN)
            }
        } else {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun onTokenReceived(account: Account, password: String, token: String) {
        val result = Bundle()
        if (am.addAccountExplicitly(account, password, Bundle())) {
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
            result.putString(AccountManager.KEY_AUTHTOKEN, token)
            am.setAuthToken(account, account.type, token)
        } else {
            result.putString(AccountManager.KEY_ERROR_MESSAGE, ACCOUNT_ALREADY_EXIST)
        }
        setAccountAuthenticatorResult(result)
        setResult(Activity.RESULT_OK)
        finish()
    }
}