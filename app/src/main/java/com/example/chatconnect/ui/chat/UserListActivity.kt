package com.example.chatconnect.ui.chat

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatconnect.SupabaseInstance
import com.example.chatconnect.adapter.UserAdapter
import com.example.chatconnect.data.model.User
import com.example.chatconnect.databinding.ActivityUserListBinding
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class UserListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserListBinding
    private lateinit var adapter: UserAdapter
    private val userList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = UserAdapter(userList) { user ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("target_uid", user.uid)
            startActivity(intent)
        }

        binding.recyclerUsers.layoutManager = LinearLayoutManager(this)
        binding.recyclerUsers.adapter = adapter

        // Load users (placeholder or Supabase)
        loadUsers()

        binding.fabRefresh.setOnClickListener {
            loadUsers()
        }
    }

    private fun loadUsers() {
        // TODO: Replace with Supabase fetch
        lifecycleScope.launch {
            try {
                val response = SupabaseInstance.client
                    .from("users")
                    .select()
                    .decodeList<User>()
                userList.clear()
                userList.addAll(
                    listOf(
                        User("aB93xTqK", "ShadowFox"),
                        User("dR82pLsV", "Midnight"),
                        User("xY63bHuP", "SilentOne")
                    )
                )
                adapter.notifyDataSetChanged()
            }catch (e: Exception){

            }
        }
    }
}
