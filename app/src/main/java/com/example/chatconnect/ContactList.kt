package com.example.chatconnect

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatconnect.Adaptor.ContactAdapter
import com.example.chatconnect.Data_Model.Contact
import com.google.firebase.database.*
import java.util.*

class ContactList : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var backBtn: ImageView
    private lateinit var adapter: ContactAdapter
    private lateinit var contactList: ArrayList<Contact>
    private lateinit var database: DatabaseReference

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) loadContacts()
            else Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_list)
        supportActionBar?.hide()

        backBtn = findViewById(R.id.back_btn)
        backBtn.setOnClickListener {
            finish()
            startActivity(Intent(this, MainActivity::class.java))
        }

        val searchBar = findViewById<EditText>(R.id.searchBar)

        recyclerView = findViewById(R.id.contactRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        contactList = ArrayList()
        adapter = ContactAdapter(this, contactList)
        recyclerView.adapter = adapter

        database = FirebaseDatabase.getInstance().getReference("users")

        // 🔍 SEARCH BAR LOGIC
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        checkPermission()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            loadContacts()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun loadContacts() {
        contactList.clear()

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                val name = cursor.getString(
                    cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                )
                var phone = cursor.getString(
                    cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                )

                phone = phone.replace("\\s+".toRegex(), "").replace("-", "")

                if (!phone.startsWith("+91") && phone.length == 10) {
                    phone = "+91$phone"
                }

                contactList.add(Contact(name, phone, false))
            }
            cursor.close()
        }

        checkRegisteredUsers()
    }

    private fun checkRegisteredUsers() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (contact in contactList) {
                    for (child in snapshot.children) {
                        val phoneInDb = child.child("phone").getValue(String::class.java)
                        val uidInDb = child.key

                        if (phoneInDb == contact.phone) {
                            contact.isRegistered = true
                            contact.uid = uidInDb
                            break
                        }
                    }
                }

                adapter.refresh()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
