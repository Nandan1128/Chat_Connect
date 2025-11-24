package com.example.chatconnect

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatconnect.adaptor.UserAdapter
import com.example.chatconnect.auth.Login
import com.example.chatconnect.data_Model.User
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var logout_btn: ImageView
    private lateinit var adapter: UserAdapter
    private lateinit var contact_list: FloatingActionButton
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbref: DatabaseReference

    // Permission launcher
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) loadUsers()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        mAuth = FirebaseAuth.getInstance()
        mDbref = FirebaseDatabase.getInstance().getReference("user")

        userList = ArrayList()
        adapter = UserAdapter(this, userList)

        userRecyclerView = findViewById(R.id.userRecyclerView)
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.adapter = adapter

        contact_list = findViewById(R.id.contact_list)
        logout_btn = findViewById(R.id.logout_btn)

        contact_list.setOnClickListener {
            startActivity(Intent(this@MainActivity, ContactList::class.java))
        }

        logout_btn.setOnClickListener {
            mAuth.signOut()
            val intent = Intent(this@MainActivity, Login::class.java)
            finish()
            startActivity(intent)
        }

        // Check contact permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            loadUsers()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    // 🔥 STEP 1: Load contacts from device
    private fun getPhoneContacts(): ArrayList<String> {
        val contactNumbers = ArrayList<String>()
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, null
        )

        if (cursor != null) {
            while (cursor.moveToNext()) {

                var phone = cursor.getString(
                    cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                )
                val name = cursor.getString(
                    cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                )

                // Normalize phone
                phone = phone.replace("\\s+".toRegex(), "")
                phone = phone.replace("-", "")

                if (!phone.startsWith("+91") && phone.length == 10) {
                    phone = "+91$phone"
                }

                contactNumbers.add(phone)
            }
            cursor.close()
        }
        return contactNumbers
    }

    // 🔥 STEP 2: Match contacts with Firebase users
    private fun loadUsers() {

        val myContacts = getPhoneContacts()  // contacts in phone

        mDbref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                userList.clear()

                for (child in snapshot.children) {

                    val user = child.getValue(User::class.java)


                    if (user != null && user.uid != mAuth.currentUser?.uid) {


                        // Normalize Firebase phone
                        var dbPhone = user.phone ?: ""
                        dbPhone = dbPhone.replace("\\s+".toRegex(), "").replace("-", "")
                        if (!dbPhone.startsWith("+91") && dbPhone.length == 10) {
                            dbPhone = "+91$dbPhone"
                        }
                        Log.d("UserDebug", "Loaded user: ${user.name}")


                        // Only show users who are in my contacts
                        if (dbPhone in myContacts) {
                            userList.add(user)
                        }
                    }
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
