package com.example.chatconnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.AlphaAnimation
import android.view.inputmethod.InputBinding
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.chatconnect.data.model.User
import com.example.chatconnect.databinding.ActivitySplashScreenBinding
import com.example.chatconnect.ui.auth.LoginActivity
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("Supabase", "URL: ${BuildConfig.SUPABASE_URL}")
        lifecycleScope.launch {
            val result = SupabaseInstance.client.from("users").select{limit(1)}.decodeList<
                    User>()
            Log.d("Supabase", "Users fetched: $result")
        }

        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration =1200
            fillAfter = true
        }

        binding.logo.startAnimation(fadeIn)


        // Navigate to next screen after delay
        binding.root.postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 2200)

    }
}