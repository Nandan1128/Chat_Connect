package com.example.chatconnect

import android.content.Intent
import android.os.Bundle
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import com.example.chatconnect.auth.Login
import com.example.chatconnect.databinding.ActivitySplashScreenBinding

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration =1200
            fillAfter = true
        }

        binding.logo.startAnimation(fadeIn)


        // Navigate to next screen after delay
        binding.root.postDelayed({
            startActivity(Intent(this, Login::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 2200)

    }
}