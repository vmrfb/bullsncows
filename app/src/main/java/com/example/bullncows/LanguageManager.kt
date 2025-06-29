package com.example.bullncows

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import java.util.*

enum class Language(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    RUSSIAN("ru", "Русский"),
    PORTUGUESE("pt", "Português"),
    FINNISH("fi", "Suomi");

    companion object {
        fun getByCode(code: String): Language {
            return values().find { it.code == code } ?: ENGLISH
        }
    }
}

object LanguageManager {
    private const val PREFS_NAME = "language_prefs"
    private const val KEY_LANGUAGE = "selected_language"
    
    private lateinit var prefs: SharedPreferences
    
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun getCurrentLanguage(): Language {
        val languageCode = prefs.getString(KEY_LANGUAGE, Language.ENGLISH.code)
        return Language.getByCode(languageCode!!)
    }
    
    fun setLanguage(language: Language, context: Context) {
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
        
        // Update locale
        val locale = Locale(language.code)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        // Create new context with updated configuration
        val newContext = context.createConfigurationContext(config)
        
        // Update resources
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
    
    fun getLanguages(): Array<Language> {
        return Language.values()
    }
} 