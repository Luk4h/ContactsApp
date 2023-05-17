package com.example.contactsapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.contactsapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var recyclerView: RecyclerView
    private lateinit var contactDatabase: ContactDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the contactDatabase
        contactDatabase = ContactDatabase(this)

        recyclerView = binding.recyclerView

        // Create mockup data
//        contactDatabase.insertContact("John Doe", listOf(PhoneNumber("1234567890", PhoneType.HOME), PhoneNumber("1234567890", PhoneType.HOME)))
//        contactDatabase.insertContact("Jane Smith", listOf(PhoneNumber("9876543210", PhoneType.MOBILE)))
        val contacts = contactDatabase.getAllContacts()
        displayContacts(contacts)

        setSupportActionBar(binding.toolbar)

        binding.fab.setOnClickListener {
            val intent = Intent(this, EditContactActivity::class.java)
            startActivity(intent)
        }
    }

    private fun displayContacts(contacts: List<Contact>) {
        val adaptedContacts = contacts.map { contactDB ->
            Contact(contactDB.id, contactDB.name, contactDB.phoneNumbers.map { phoneNumberDB ->
                PhoneNumber(phoneNumberDB.number, phoneNumberDB.type)
            })
        }
        val sortedContacts = adaptedContacts.sortedBy { it.name }
        val adapter = ContactAdapter(sortedContacts)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            Log.d("com.example.contactsapp", "Updated Contacts list")
            // Contact has been edited, fetch the updated contact list
            updateContactsList()
        }
    }

    private fun updateContactsList() {
        val contacts = contactDatabase.getAllContacts()
        displayContacts(contacts)
    }
}