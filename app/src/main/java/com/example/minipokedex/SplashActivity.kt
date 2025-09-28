package com.example.minipokedex

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen") // Suppress a lint warning for custom splash screens
class SplashActivity : AppCompatActivity() {

    private val SPLASH_DELAY: Long = 3000 // 3 segundos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Finaliza a SplashActivity para que o usuário não volte para ela
        }, SPLASH_DELAY)
    }
}