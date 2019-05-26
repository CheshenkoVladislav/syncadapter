package com.example.syncadapterapplication

import android.accounts.Account
import android.content.*
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import android.provider.ContactsContract.RawContacts
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.CommonDataKinds.Email
import android.content.ContentValues
import android.content.ContentProviderOperation
import android.provider.ContactsContract.CommonDataKinds.Phone
import java.util.regex.Pattern


class ContactsManager(val resolver: ContentResolver) {

    companion object {
        const val MIMETYPE = "vnd.android.cursor.item/com.example.syncadapterapplication"
        const val TAG = "ContactsManager"
    }

    fun addAccountFieldToContact(account: Account, phoneNumber: String) {
        val op = ArrayList<ContentProviderOperation>()

        var numberTest = phoneNumber
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(numberTest))
        val cursor = resolver.query(uri,
            arrayOf(ContactsContract.Data.CONTACT_ID, ContactsContract.Data.DISPLAY_NAME),
            null, null, null)
        if (cursor.moveToFirst()) {
            Log.d(TAG, "Start edit contact")
            val id = cursor.getString(0)

            /* Создаем необработанный контакт */
            op.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                .withValue(RawContacts.ACCOUNT_TYPE, account.type)
                .withValue(RawContacts.ACCOUNT_NAME, account.name)
                .build())
            /* Добавляем данные имени */
            op.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                .withValue(StructuredName.DISPLAY_NAME, cursor.getString(1))
                .build())
            /* Добавляем данные телефона */
            op.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                .withValue(Phone.NUMBER, numberTest)
                .withValue(Phone.TYPE, Phone.TYPE_MOBILE)
                .build())

            /* Добавляем кастомное поле */
            op.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, MIMETYPE)
                .withValue(ContactsContract.Data.DATA1, 123456)
                .withValue(ContactsContract.Data.DATA2, "sample")
                .withValue(ContactsContract.Data.DATA3, "Detail sample culumn")
                .build())

            /* Принудительно мерджим контакты по айди */
            op.add(ContentProviderOperation.newUpdate(ContactsContract.AggregationExceptions.CONTENT_URI)
                .withValue(ContactsContract.AggregationExceptions.TYPE,
                    ContactsContract.AggregationExceptions.TYPE_KEEP_TOGETHER)
                .withValue(ContactsContract.AggregationExceptions.RAW_CONTACT_ID1, 0)
                .withValue(ContactsContract.AggregationExceptions.RAW_CONTACT_ID2, id)
                .build())

            try {
                resolver.applyBatch(ContactsContract.AUTHORITY, op)
            } catch (e: Exception) {
                Log.e("Exception: ", e.message)
            }
        }

    }

    /* Если обращаемся к книге контактов из адаптера, то нужно добавлять пометку к Uri каждого запроса */
    private fun addCallerIsSyncAdapterParameter(uri: Uri, isSyncOperation: Boolean): Uri {
        return if (isSyncOperation) {
            uri.buildUpon()
                .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                .build()
        } else uri
    }
}
