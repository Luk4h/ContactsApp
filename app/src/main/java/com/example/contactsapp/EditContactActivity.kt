package com.example.contactsapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.contactsapp.databinding.ActivityEditContactBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class EditContactActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditContactBinding
    private lateinit var contactDatabase: ContactDatabase
    private var contact: Contact? = null
    private var contactId: Long = -1 // Default value

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the contactDatabase
        contactDatabase = ContactDatabase(this)

        // Retrieve the contact id from the intent extras
        contactId = intent.getLongExtra("contactId", -1)

        // Fetch the contact data from the database
        contact = contactDatabase.getContactById(contactId)
        contact?.let { existingContact ->
            binding.nameEditText.setText(existingContact.name)
            for (phoneNumber in existingContact.phoneNumbers) {
                addPhoneNumberField(phoneNumber.number, phoneNumber.type)
            }
        }

        // Set a click listener for the save button
        binding.addPhoneNumberButton.setOnClickListener {
            addPhoneNumberField("", PhoneType.HOME)
        }

        // Set a click listener for the save button
        binding.saveButton.setOnClickListener {
            if (contact == null) {
                createContact()
            } else {
                saveContact()
            }
        }

        // Set a click listener for the delete button
        binding.deleteButton.setOnClickListener {
            deleteContact()
        }
    }

    private fun saveContact() {
        val name = binding.nameEditText.text.toString()

        // Validate name field
        if (name.isEmpty()) {
            binding.nameInputLayout.error = "Please enter a name"
            return
        } else {
            binding.nameInputLayout.error = null
        }

        val phoneNumbers = mutableListOf<PhoneNumber>()
        for (i in 0 until binding.phoneNumberLayout.childCount) {
            val phoneNumberLinearLayout = binding.phoneNumberLayout.getChildAt(i) as LinearLayout
            val phoneNumberTextInputLayout = phoneNumberLinearLayout.getChildAt(0) as TextInputLayout
            val phoneNumberEditText = phoneNumberTextInputLayout.editText
            val phoneNumber = phoneNumberEditText?.text.toString()

            // Validate phone number field
            if (phoneNumber.isEmpty()) {
                phoneNumberTextInputLayout.error = "Please enter a phone number"
                return
            } else {
                phoneNumberTextInputLayout.error = null
            }

            val phoneNumberTypeSpinner = phoneNumberLinearLayout.getChildAt(1) as Spinner
            val type = when (phoneNumberTypeSpinner.selectedItem.toString()) {
                "Home" -> PhoneType.HOME
                "Mobile" -> PhoneType.MOBILE
                "Work" -> PhoneType.WORK
                else -> throw IllegalArgumentException("Invalid phone type")
            }

            phoneNumbers.add(PhoneNumber(phoneNumber, type))
        }

        // Update the existing contact with the edited data
        contact?.let {
            val newContact =  Contact(it.id, name, phoneNumbers)
            contactDatabase.editContact(newContact)
        }

        // Create a new intent to send the updated contact back to MainActivity
        val resultIntent = Intent().apply {
            putExtra("contact", contact?.let { Contact(it.id, name, phoneNumbers) })
            putExtra("position", intent.getIntExtra("position", -1))
        }

        setResult(Activity.RESULT_OK, resultIntent)
        Log.d("com.example.contactsapp", "Updated Contacts list")
        finish() // Finish the activity after saving the contact
    }

    private fun createContact() {
        val name = binding.nameEditText.text.toString()

        // Validate name field
        if (name.isEmpty()) {
            binding.nameInputLayout.error = "Please enter a name"
            return
        } else {
            binding.nameInputLayout.error = null
        }

        val phoneNumbers = mutableListOf<PhoneNumber>()
        for (i in 0 until binding.phoneNumberLayout.childCount) {
            val phoneNumberLinearLayout = binding.phoneNumberLayout.getChildAt(i) as LinearLayout
            val phoneNumberTextInputLayout = phoneNumberLinearLayout.getChildAt(0) as TextInputLayout
            val phoneNumberEditText = phoneNumberTextInputLayout.editText
            val phoneNumber = phoneNumberEditText?.text.toString()

            // Validate phone number field
            if (phoneNumber.isEmpty()) {
                phoneNumberTextInputLayout.error = "Please enter a phone number"
                return
            } else {
                phoneNumberTextInputLayout.error = null
            }

            val phoneNumberTypeSpinner = phoneNumberLinearLayout.getChildAt(1) as Spinner
            val type = when (phoneNumberTypeSpinner.selectedItem.toString()) {
                "Home" -> PhoneType.HOME
                "Mobile" -> PhoneType.MOBILE
                "Work" -> PhoneType.WORK
                else -> throw IllegalArgumentException("Invalid phone type")
            }

            phoneNumbers.add(PhoneNumber(phoneNumber, type))
        }

        // Update the existing contact with the edited data
        val contactId = contactDatabase.insertContact(name, phoneNumbers)

        // Create a new intent to send the updated contact back to MainActivity
        val resultIntent = Intent().apply {
            putExtra("contact", contact?.let { Contact(it.id, name, phoneNumbers) })
            putExtra("position", intent.getIntExtra("position", -1))
        }

        setResult(Activity.RESULT_OK, resultIntent)
        Log.d("com.example.contactsapp", "Updated Contacts list")
        finish() // Finish the activity after saving the contact
    }

    private fun deleteContact() {
        contactDatabase.deleteContact(contactId)

        // Create a new intent to send the updated contact back to MainActivity
        val resultIntent = Intent().apply {
            putExtra("contact", contact?.let { contact })
            putExtra("position", intent.getIntExtra("position", -1))
        }

        setResult(Activity.RESULT_OK, resultIntent)
        Log.d("com.example.contactsapp", "Updated Contacts list")
        finish() // Finish the activity after saving the contact
    }

    private fun addPhoneNumberField(number: String, type: PhoneType) {
        val phoneNumberView = layoutInflater.inflate(R.layout.item_phone_number, binding.phoneNumberLayout, false)
        val phoneNumberEditText = phoneNumberView.findViewById<TextInputEditText>(R.id.phoneNumberEditText)
        val phoneNumberTypeSpinner = phoneNumberView.findViewById<Spinner>(R.id.phoneTypeSpinner)

        val phoneTypeAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.phone_types,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        phoneNumberTypeSpinner.adapter = phoneTypeAdapter

        phoneNumberEditText.setText(number)
        phoneNumberTypeSpinner.setSelection(type.ordinal)

        binding.phoneNumberLayout.addView(phoneNumberView)
    }
}
