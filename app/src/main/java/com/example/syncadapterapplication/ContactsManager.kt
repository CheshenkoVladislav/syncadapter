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


class ContactsManager(private val context: Context) {

    companion object {
        const val MIMETYPE = "vnd.android.cursor.item/com.example.syncadapterapplication"
        const val TAG = "ContactsManager"
    }

    fun addContact(account: Account, context: Context,
                   contact: Contact) { // My Contact object is a custom object made by you
        val resolver = context.contentResolver
        resolver.delete(RawContacts.CONTENT_URI,
            RawContacts.ACCOUNT_TYPE + " = ?",
            arrayOf(account.type))

        val ops = ArrayList<ContentProviderOperation>()

        /**
         * this is very important, if you want to add this raw contact to a contact (please refer to android contact provider guide in order to get the difference)
         * my advise is you to add contact id manually, even though most times android does this automatically.
         */
        val contact_id = 1234

        ops.add(ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(RawContacts.CONTENT_URI, true))
            .withValue(RawContacts.ACCOUNT_NAME, account.name)
            .withValue(RawContacts.ACCOUNT_TYPE, account.type)
            .withValue(ContactsContract.RawContacts.CONTACT_ID, contact_id)
            .build())

        // this is for display name
        ops.add(
            ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(ContactsContract.Settings.CONTENT_URI,
                true))
                .withValue(RawContacts.ACCOUNT_NAME, account.name)
                .withValue(RawContacts.ACCOUNT_TYPE, account.type)
                .withValue(ContactsContract.Settings.UNGROUPED_VISIBLE, 1)
                .build())

        ops.add(ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI,
            true))
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
            .withValue(StructuredName.GIVEN_NAME, contact.name)
            .withValue(StructuredName.FAMILY_NAME, "chesh")
            .build())
        //
        ops.add(ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI,
            true))
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, "12342145")
            .build())
        //
        //
        ops.add(ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI,
            true))
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.Email.DATA, "sample@email.com")
            .build())

        //        This is our custom data field in our contact
        ops.add(ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI,
            true))
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE, MIMETYPE)
            .withValue(ContactsContract.Data.DATA1, 12345)
            .withValue(ContactsContract.Data.DATA2, "sample")
            .withValue(ContactsContract.Data.DATA3, "Detail sample culumn")
            .build())
        try {
            val results = resolver.applyBatch(ContactsContract.AUTHORITY, ops)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun addAccountFieldToContact(account: Account, phoneNumber: String) {
        val op = ArrayList<ContentProviderOperation>()

        var numberTest = phoneNumber
        //        numberTest = "12342145"
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(numberTest))
        val resolver = context.contentResolver
        val cursor = resolver.query(uri,
            arrayOf(ContactsContract.Data.CONTACT_ID, ContactsContract.Data.DISPLAY_NAME),
            null, null, null)
        Log.d(TAG, "Start edit contact")
        if (cursor.moveToFirst()) {
            val id = cursor.getString(0)
            //            Log.e(TAG, "DATA3 = " + cursor.getString(1))
            //            Log.e(TAG, "MIMETYPE = " + cursor.getString(2))
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
            //
            op.add(ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI,
                true))
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, MIMETYPE)
                .withValue(ContactsContract.Data.DATA1, 123456)
                .withValue(ContactsContract.Data.DATA2, "sample")
                .withValue(ContactsContract.Data.DATA3, "Detail sample culumn")
                .build())

            /* Добавляем пустой контакт */

            op.add(ContentProviderOperation.newUpdate(ContactsContract.AggregationExceptions.CONTENT_URI)
                .withValue(ContactsContract.AggregationExceptions.TYPE,
                    ContactsContract.AggregationExceptions.TYPE_KEEP_TOGETHER)
                .withValue(ContactsContract.AggregationExceptions.RAW_CONTACT_ID1, 0)
                .withValue(ContactsContract.AggregationExceptions.RAW_CONTACT_ID2, id)
                .build())

            try {
                context.contentResolver.applyBatch(ContactsContract.AUTHORITY, op)

            } catch (e: Exception) {
                Log.e("Exception: ", e.message)
            }
        }

    }

    private fun addCallerIsSyncAdapterParameter(uri: Uri, isSyncOperation: Boolean): Uri {
        return if (isSyncOperation) {
            uri.buildUpon()
                .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                .build()
        } else uri
    }

    fun getMyContacts(): List<Contact>? {
        return null
    }


    fun updateMyContact(number: String, account: Account) {
        Log.d(TAG, "Start updateMyContact()")
        var numberTest = "+79199524015"
        //        numberTest = "12342145"
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(numberTest))
        val resolver = context.contentResolver
        val cursor = resolver.query(uri, arrayOf(ContactsContract.Data.CONTACT_ID), null, null, null)
        Log.d(TAG, "Start edit contact")
        cursor.moveToFirst()
        val id = cursor.getString(0)

        val rawContactId = ContentUris.parseId(uri)
        val operations = arrayListOf<ContentProviderOperation>()

        val values = ContentValues()

        addRawContact("fsaf", "+79161357724", "sdasf", id, account)
        //        updateContact("fsaf", "+79161357724", "sdasf", id)
    }

    fun update(id: Int) {
        val id = 1
        val firstname = "Contact's first name"
        val lastname = "Last name"
        val number = "000 000 000"
        val photo_uri = "android.resource://com.my.package/drawable/default_photo"

        val resolver = context.contentResolver
        val ops = ArrayList<ContentProviderOperation>()

        // Name
        var builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
        builder.withSelection(ContactsContract.Data.CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + "=?",
            arrayOf(
                id.toString(), ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE))
        builder.withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, lastname)
        builder.withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstname)
        ops.add(builder.build())

        // Number
        builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
        builder.withSelection(
            ContactsContract.Data.CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + "=?" + " AND " + ContactsContract.CommonDataKinds.Organization.TYPE + "=?",
            arrayOf(
                id.toString(), ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Phone.TYPE_HOME.toString()))
        builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
        ops.add(builder.build())


        // Update
        try {
            resolver.applyBatch(ContactsContract.AUTHORITY, ops)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun addRawContact(name: String, number: String, email: String, ContactId: String,
                      account: Account) {
        val resolver = context.contentResolver
        val where = ContactsContract.Data.CONTACT_ID + " = ?"
        val ops = arrayListOf<ContentProviderOperation>()

        ops.add(ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(RawContacts.CONTENT_URI, true))
            .withValue(RawContacts.ACCOUNT_NAME, account.name)
            .withValue(RawContacts.ACCOUNT_TYPE, account.type)
            .withValue(ContactsContract.RawContacts.CONTACT_ID, ContactId)
            .build())

        ops.add(ContentProviderOperation.newInsert(
            addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI, true))
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE, MIMETYPE)
            .withValue(ContactsContract.Data.DATA1, 12345)
            .withValue(ContactsContract.Data.DATA2, "sample")
            .withValue(ContactsContract.Data.DATA3, "Detail sample culumn")
            .build())
        val res = resolver.applyBatch(ContactsContract.AUTHORITY, ops)
        Log.e(TAG, res.size.toString())
    }

    fun updateContact(name: String, number: String, email: String, ContactId: String): Boolean {
        var name = name
        var number = number
        var email = email
        val mime = "samplemime"
        var success = true
        val phnumexp = "^[0-9]*$"

        try {
            name = name.trim { it <= ' ' }
            email = email.trim { it <= ' ' }
            number = number.trim { it <= ' ' }

            if (name == "" && number == "" && email == "") {
                success = false
            } else {
                val contentResolver = context.getContentResolver()

                val where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?"

                val emailParams = arrayOf(ContactId, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                val nameParams = arrayOf(ContactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                val numberParams = arrayOf(ContactId, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                val mimeParams = arrayOf(ContactId, ContactsContract.Data.DATA3)

                val ops = ArrayList<ContentProviderOperation>()

                if (email != "") {
                    ops.add(ContentProviderOperation.newUpdate(
                        android.provider.ContactsContract.Data.CONTENT_URI)
                        .withSelection(where, emailParams)
                        .withValue(Email.DATA, email)
                        .build())
                }

                if (name != "") {
                    ops.add(ContentProviderOperation.newUpdate(
                        android.provider.ContactsContract.Data.CONTENT_URI)
                        .withSelection(where, nameParams)
                        .withValue(StructuredName.DISPLAY_NAME, name)
                        .build())
                }

                if (number != "") {

                    ops.add(ContentProviderOperation.newUpdate(
                        android.provider.ContactsContract.Data.CONTENT_URI)
                        .withSelection(where, numberParams)
                        .withValue(Phone.NUMBER, number)
                        .build())
                }
                contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            success = false
        }
        return success
    }


    // To get COntact Ids of all contact use the below method

    /**
     * @return arraylist containing id's  of all contacts <br></br>
     * empty arraylist if no contacts exist <br></br><br></br>
     * **Note: **This method requires permission **android.permission.READ_CONTACTS**
     */
    fun getAllConactIds(): ArrayList<String> {
        val contactList = ArrayList<String>()


        val cursor =
            context.contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, "display_name ASC")

        if (cursor != null) {
            if (cursor!!.moveToFirst()) {
                do {
                    val _id = cursor!!.getInt(cursor!!.getColumnIndex("_id"))
                    contactList.add("" + _id)

                } while (cursor!!.moveToNext())
            }
        }

        return contactList
    }


    private fun isEmailValid(email: String): Boolean {
        val emailAddress = email.trim { it <= ' ' }
        if (emailAddress == null)
            return false
        else if (emailAddress == "")
            return false
        else if (emailAddress.length <= 6)
            return false
        else {
            val expression =
                "^[a-z][a-z|0-9|]*([_][a-z|0-9]+)*([.][a-z|0-9]+([_][a-z|0-9]+)*)?@[a-z][a-z|0-9|]*\\.([a-z][a-z|0-9]*(\\.[a-z][a-z|0-9]*)?)$"
            val pattern = Pattern.compile(expression,
                Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(emailAddress)
            return if (matcher.matches())
                true
            else
                false
        }
    }

    private fun match(stringToCompare: String, regularExpression: String): Boolean {
        var success = false
        val pattern = Pattern.compile(regularExpression)
        val matcher = pattern.matcher(stringToCompare)
        if (matcher.matches())
            success = true
        return success
    }

}
