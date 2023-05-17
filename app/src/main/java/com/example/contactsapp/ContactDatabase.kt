package com.example.contactsapp
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ContactDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "ContactDatabase.db"

        private object Contacts {
            const val TABLE_NAME = "contacts"
            const val COLUMN_ID = "id"
            const val COLUMN_NAME = "name"
        }
        private object PhoneNumbers {
            const val TABLE_NAME = "phone_numbers"
            const val COLUMN_ID = "id"
            const val COLUMN_CONTACT_ID = "contact_id"
            const val COLUMN_NUMBER = "number"
            const val COLUMN_TYPE = "type"
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createContactsTableQuery = "CREATE TABLE ${Contacts.TABLE_NAME} " +
                "(${Contacts.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "${Contacts.COLUMN_NAME} TEXT)"
        db.execSQL(createContactsTableQuery)
        val createPhoneNumbersTableQuery = "CREATE TABLE ${PhoneNumbers.TABLE_NAME} " +
                "(${PhoneNumbers.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "${PhoneNumbers.COLUMN_CONTACT_ID} INTEGER, " +
                "${PhoneNumbers.COLUMN_NUMBER} TEXT, " +
                "${PhoneNumbers.COLUMN_TYPE} TEXT, " +
                "FOREIGN KEY(${PhoneNumbers.COLUMN_CONTACT_ID}) REFERENCES ${Contacts.TABLE_NAME}(${Contacts.COLUMN_ID}))"
        db.execSQL(createPhoneNumbersTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    fun insertContact(name: String, phoneNumbers: List<PhoneNumber>): Long {
        val db = writableDatabase

        // Insert the contact into the contact table
        val contactValues = ContentValues().apply {
            put(Contacts.COLUMN_NAME, name)
        }
        val contactId = db.insert(Contacts.TABLE_NAME, null, contactValues)

        // Insert phone numbers into the phone number table
        val phoneNumbers = phoneNumbers
        for (phoneNumber in phoneNumbers) {
            val phoneNumberValues = ContentValues().apply {
                put(PhoneNumbers.COLUMN_CONTACT_ID, contactId)
                put(PhoneNumbers.COLUMN_NUMBER, phoneNumber.number)
                put(PhoneNumbers.COLUMN_TYPE, phoneNumber.type.name)
            }
            db.insert(PhoneNumbers.TABLE_NAME, null, phoneNumberValues)
        }

        db.close()

        return contactId
    }

    fun getAllContacts(): List<Contact> {
        val db = readableDatabase
        val contacts = mutableListOf<Contact>()

        val selectQuery = "SELECT * FROM ${Contacts.TABLE_NAME}"
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val contactId = cursor.getColumnIndex(Contacts.COLUMN_ID)
                val contactIdLong = cursor.getLong(contactId)
                val contactName = cursor.getColumnIndex(Contacts.COLUMN_NAME)
                val contactNameString = cursor.getString(contactName)

                val phoneNumbers = getPhoneNumbersForContact(contactIdLong)
                val contact = Contact(contactIdLong, contactNameString, phoneNumbers)
                contacts.add(contact)
            } while (cursor.moveToNext())
        }

        cursor?.close()
        return contacts
    }

    private fun getPhoneNumbersForContact(contactId: Long): List<PhoneNumber> {
        val db = readableDatabase
        val phoneNumbers = mutableListOf<PhoneNumber>()

        val selectQuery = "SELECT * FROM ${PhoneNumbers.TABLE_NAME} WHERE ${PhoneNumbers.COLUMN_CONTACT_ID} = $contactId"
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val numberIdx = cursor.getColumnIndex(PhoneNumbers.COLUMN_NUMBER)
                val numberString = cursor.getString(numberIdx)
                val typeStringIdx = cursor.getColumnIndex(PhoneNumbers.COLUMN_TYPE)
                val typeString = cursor.getString(typeStringIdx)
                val type = PhoneType.valueOf(typeString)
                val phoneNumber = PhoneNumber(numberString, type)
                phoneNumbers.add(phoneNumber)
            } while (cursor.moveToNext())
        }

        cursor?.close()
        return phoneNumbers
    }

    fun deleteContact(contactId: Long) {
        val db = writableDatabase
        db.delete(PhoneNumbers.TABLE_NAME, "${PhoneNumbers.COLUMN_CONTACT_ID}=?", arrayOf(contactId.toString()))
        db.delete(Contacts.TABLE_NAME, "${Contacts.COLUMN_ID}=?", arrayOf(contactId.toString()))
    }
    fun editContact(contact: Contact) {
        val db = writableDatabase
        val contactValues = ContentValues().apply {
            put(Contacts.COLUMN_NAME, contact.name)
        }
        val contactId = db.update(Contacts.TABLE_NAME, contactValues, "${Contacts.COLUMN_ID}=?", arrayOf(contact.id.toString()))
        db.delete(PhoneNumbers.TABLE_NAME, "${PhoneNumbers.COLUMN_CONTACT_ID}=?", arrayOf(contact.id.toString()))
        for (phoneNumber in contact.phoneNumbers) {
            val phoneNumberValues = ContentValues().apply {
                put(PhoneNumbers.COLUMN_CONTACT_ID, contact.id)
                put(PhoneNumbers.COLUMN_NUMBER, phoneNumber.number)
                put(PhoneNumbers.COLUMN_TYPE, phoneNumber.type.toString())
            }
            db.insert(PhoneNumbers.TABLE_NAME, null, phoneNumberValues)
        }
    }

    fun getContactById(contactId: Long): Contact? {
        val db = readableDatabase

        val selectQuery = "SELECT * FROM ${Contacts.TABLE_NAME} WHERE ${Contacts.COLUMN_ID} = $contactId"
        val cursor = db.rawQuery(selectQuery, null)

        var contact: Contact? = null

        if (cursor != null && cursor.moveToFirst()) {
            val idIndex = cursor.getColumnIndex(Contacts.COLUMN_ID)
            val nameIndex = cursor.getColumnIndex(Contacts.COLUMN_NAME)

            val id = cursor.getLong(idIndex)
            val name = cursor.getString(nameIndex)

            // Fetch phone numbers for the contact
            val phoneNumberList = getPhoneNumbersForContact(contactId)

            contact = Contact(id, name, phoneNumberList)
        }

        cursor?.close()
        return contact
    }
//
//    fun editContact(contactId: Long, name: String, phoneNumbers: List<PhoneNumber>) {
//        val db = writableDatabase
//
//        val contactValues = ContentValues().apply {
//            put(COLUMN_NAME, name)
//        }
//        db.update(TABLE_CONTACTS, contactValues, "$COLUMN_ID=?", arrayOf(contactId.toString()))
//
//        db.delete(getPhoneNumberTableName(contactId), null, null)
//
//        for (phoneNumber in phoneNumbers) {
//            val phoneNumberValues = ContentValues().apply {
//                put(COLUMN_NUMBER, phoneNumber.number)
//                put(COLUMN_TYPE, phoneNumber.type.toString())
//            }
//            db.insert(getPhoneNumberTableName(contactId), null, phoneNumberValues)
//        }
//    }

}
