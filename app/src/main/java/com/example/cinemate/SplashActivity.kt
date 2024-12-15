package com.example.cinemate

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.cinemate.MainActivity
import com.example.cinemate.R
import com.example.cinemate.auth.LoginActivity
import com.example.cinemate.auth.PrefManager
import com.example.cinemate.onboarding.OnboardingActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var prefManager: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Inisialisasi PrefManager
        prefManager = PrefManager(this)

        // Delay untuk menampilkan splash screen
        Handler().postDelayed({
            if (prefManager.isLoggedIn()) {
                // Jika sudah login, arahkan ke MainActivity
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // Jika belum login, arahkan ke OnboardingActivity
                startActivity(Intent(this, OnboardingActivity::class.java))
            }
            finish() // Tutup SplashScreenActivity
        }, 2000) // Tampilkan splash screen selama 2 detik
    }
}