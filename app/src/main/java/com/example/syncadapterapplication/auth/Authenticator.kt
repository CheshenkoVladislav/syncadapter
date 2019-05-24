package com.example.syncadapterapplication.auth

import android.accounts.*
import android.content.Context
import android.os.Bundle
import android.provider.SyncStateContract
import android.content.Intent
import android.text.TextUtils

class Authenticator(private val context: Context) : AbstractAccountAuthenticator(context) {

    companion object {
        const val AUTH_TOKEN = "myToken2"
    }

    override fun getAuthTokenLabel(authTokenType: String?): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun confirmCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        options: Bundle?
    ): Bundle {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAuthToken(
        response: AccountAuthenticatorResponse?, account: Account?, authTokenType: String?, options: Bundle?
    ): Bundle {
        val result = Bundle()
        val accountManager = AccountManager.get(context.applicationContext)
        var authToken = accountManager.peekAuthToken(account, authTokenType)
        if (TextUtils.isEmpty(authToken)) {
            val password = accountManager.getPassword(account)
            if (!TextUtils.isEmpty(password)) authToken = AUTH_TOKEN
        }
        if (!TextUtils.isEmpty(authToken) && account != null) {
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken)
        } else {
            val intent = Intent(context, AuthActivity::class.java)
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            intent.putExtra(AuthActivity.EXTRA_TOKEN_TYPE, authTokenType)
            val bundle = Bundle()
            bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        }
        return result
    }

    override fun hasFeatures(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        features: Array<out String>?
    ): Bundle {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun editProperties(response: AccountAuthenticatorResponse?, accountType: String?): Bundle {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAccount(
        response: AccountAuthenticatorResponse?, accountType: String?, authTokenType: String?,
        requiredFeatures: Array<out String>?, options: Bundle?
    ): Bundle {
        val intent = Intent(context, AuthActivity::class.java)
        intent.putExtra(AuthActivity.EXTRA_TOKEN_TYPE, accountType)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)

        val bundle = Bundle()
        if (options != null) {
            bundle.putAll(options)
        }
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        return bundle
    }
}