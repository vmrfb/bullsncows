package com.example.bullncows

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bullncows.databinding.ActivityHelpBinding
import java.util.Locale

class HelpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHelpBinding

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
        val localeCode = prefs.getString("selected_language", "ru") ?: "ru"
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(Locale(localeCode))
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
} 