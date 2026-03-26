package com.example.contactformapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var rvContacts: RecyclerView
    private lateinit var etSearch: TextInputEditText
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var btnSortAZ: MaterialButton
    private lateinit var btnSortZA: MaterialButton
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var tvContactCount: TextView
    private lateinit var adapter: ContactAdapter

    companion object {
        val contactList: MutableList<Contact> = mutableListOf()
        var isSeeded: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvContacts     = findViewById(R.id.rvContacts)
        etSearch       = findViewById(R.id.etSearch)
        fabAdd         = findViewById(R.id.fabAdd)
        btnSortAZ      = findViewById(R.id.btnSortAZ)
        btnSortZA      = findViewById(R.id.btnSortZA)
        layoutEmpty    = findViewById(R.id.layoutEmpty)
        tvContactCount = findViewById(R.id.tvContactCount)

        // Seed sample contacts only once
        if (!isSeeded) {
            isSeeded = true
            contactList.addAll(listOf(
                Contact(name = "Ali Hassan",  phone = "0300-1234567", email = "ali@email.com",  address = "Lahore, Pakistan"),
                Contact(name = "Sara Khan",   phone = "0311-9876543", email = "sara@email.com", address = "Karachi, Pakistan"),
                Contact(name = "Usman Malik", phone = "0321-5556677", email = "usman@email.com",address = "Islamabad, Pakistan")
            ))
        }

        adapter = ContactAdapter(
            contactList,
            onEdit   = { contact -> openAddEdit(contact) },
            onDelete = { contact -> confirmDelete(contact) }
        )

        rvContacts.layoutManager = LinearLayoutManager(this)
        rvContacts.adapter = adapter

        refreshUI()

        // Live search
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filterContacts(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnSortAZ.setOnClickListener {
            contactList.sortBy { it.name.lowercase() }
            filterContacts(etSearch.text.toString())
        }

        btnSortZA.setOnClickListener {
            contactList.sortByDescending { it.name.lowercase() }
            filterContacts(etSearch.text.toString())
        }

        fabAdd.setOnClickListener { openAddEdit(null) }
    }

    override fun onResume() {
        super.onResume()
        refreshUI()
    }

    private fun openAddEdit(contact: Contact?) {
        val intent = Intent(this, AddContactActivity::class.java)
        contact?.let { intent.putExtra("contact", it) }
        startActivity(intent)
    }

    private fun confirmDelete(contact: Contact) {
        AlertDialog.Builder(this)
            .setTitle("Delete Contact")
            .setMessage("Are you sure you want to delete ${contact.name}?")
            .setPositiveButton("Delete") { _, _ ->
                contactList.remove(contact)
                refreshUI()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun filterContacts(query: String) {
        val filtered = if (query.isEmpty()) {
            contactList.toMutableList()
        } else {
            contactList.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.phone.contains(query)
            }.toMutableList()
        }
        adapter.updateList(filtered)
        updateEmptyState(filtered.isEmpty())
        tvContactCount.text = "${contactList.size} contacts"
    }

    private fun refreshUI() {
        adapter.updateList(contactList)
        tvContactCount.text = "${contactList.size} contacts"
        updateEmptyState(contactList.isEmpty())
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        rvContacts.visibility  = if (isEmpty) View.GONE   else View.VISIBLE
    }
}