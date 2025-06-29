package com.example.bullncows

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bullncows.adapter.AttemptsAdapter
import com.example.bullncows.data.AppDatabase
import com.example.bullncows.data.GameRecord
import com.example.bullncows.databinding.ActivityMainBinding
import com.example.bullncows.databinding.DialogWinnerBinding
import com.example.bullncows.game.BullCowsGame
import kotlinx.coroutines.launch
import java.util.*
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import kotlin.math.PI
import kotlin.math.sin
import android.widget.FrameLayout
import androidx.core.view.doOnLayout
import android.view.View
import android.content.Context
import android.view.Menu
import android.view.MenuItem

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var game: BullCowsGame
    private lateinit var attemptsAdapter: AttemptsAdapter
    private lateinit var database: AppDatabase
    private var showWinnerDialog = false

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
        
        // Initialize language manager and apply saved language
        LanguageManager.init(this)
        applySavedLanguage()
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.subtitle = getString(R.string.game_subtitle)

        database = AppDatabase.getDatabase(this)
        game = BullCowsGame()
        setupRecyclerView()
        setupInputValidation()
        setupClickListeners()
        
        // Restore game state if available
        if (savedInstanceState != null) {
            val gameState = savedInstanceState.getString("game_state")
            showWinnerDialog = savedInstanceState.getBoolean("show_winner_dialog", false)
            if (gameState != null) {
                game.restoreFromState(gameState)
                updateAttemptsList()
                if (game.isGameWon()) {
                    setGameFinishedState()
                    if (showWinnerDialog) {
                        showWinnerDialog()
                    }
                } else {
                    setGameActiveState()
                }
            } else {
                startNewGame()
            }
        } else {
            startNewGame()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save game state to prevent new game on orientation change
        outState.putString("game_state", game.getGameState())
        outState.putBoolean("show_winner_dialog", showWinnerDialog)
    }

    private fun applySavedLanguage() {
        val currentLanguage = LanguageManager.getCurrentLanguage()
        val locale = Locale(currentLanguage.code)
        Locale.setDefault(locale)
        
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        
        createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun setupRecyclerView() {
        attemptsAdapter = AttemptsAdapter()
        binding.rvAttempts.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = attemptsAdapter
        }
    }

    private fun setupInputValidation() {
        binding.etGuess.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (text.length > 4) {
                    binding.etGuess.setText(text.substring(0, 4))
                    binding.etGuess.setSelection(4)
                }
            }
        })
    }

    private fun setupClickListeners() {
        binding.btnSuggest.setOnClickListener {
            val guess = binding.etGuess.text.toString()
            if (guess.isNotEmpty()) {
                makeGuess(guess)
            }
        }

        binding.btnMenu.setOnClickListener {
            showActionMenu()
        }

        binding.btnLanguage.setOnClickListener {
            showLanguageMenu()
        }
    }

    private fun showActionMenu() {
        val options = arrayOf(
            getString(R.string.new_game),
            getString(R.string.leaderboard),
            getString(R.string.help),
            getString(R.string.quit)
        )

        AlertDialog.Builder(this)
            .setTitle("Actions")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> startNewGame()
                    1 -> startActivity(Intent(this, LeaderboardActivity::class.java))
                    2 -> startActivity(Intent(this, HelpActivity::class.java))
                    3 -> showQuitConfirmation()
                }
            }
            .show()
    }

    private fun showLanguageMenu() {
        val languages = LanguageManager.getLanguages()
        val languageNames = languages.map { it.displayName }.toTypedArray()
        val currentLanguage = LanguageManager.getCurrentLanguage()
        val currentIndex = languages.indexOf(currentLanguage)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.language))
            .setSingleChoiceItems(languageNames, currentIndex) { dialog, which ->
                val selectedLanguage = languages[which]
                if (selectedLanguage != currentLanguage) {
                    LanguageManager.setLanguage(selectedLanguage, this)
                    Toast.makeText(this, "Language changed to ${selectedLanguage.displayName}", Toast.LENGTH_SHORT).show()
                    recreate()
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun animateBullsAndCows(bulls: Int, cows: Int) {
        val overlay = binding.root.findViewById<ViewGroup>(R.id.animationOverlay)
        val icons = mutableListOf<ImageView>()
        val total = bulls + cows
        val iconSize = resources.getDimensionPixelSize(R.dimen.bull_cow_icon_size)
        val handler = Handler(Looper.getMainLooper())
        val duration = 3500L

        // Get toolbar height
        val toolbar = findViewById<View>(R.id.toolbar)
        val toolbarHeight = toolbar?.height ?: 0
        val endY = toolbarHeight + iconSize

        // Get the full screen height (excluding keyboard)
        val screenHeight = windowManager.currentWindowMetrics.bounds.height()
        val startY = screenHeight - iconSize

        overlay.doOnLayout {
            val screenWidth = overlay.width
            val saluteSpread = (screenWidth * 0.12f).coerceAtLeast(80f) // spread factor for salute

            fun createIcon(resId: Int, index: Int) {
                val imageView = ImageView(this)
                imageView.setImageResource(resId)
                imageView.alpha = 0.7f
                val params = FrameLayout.LayoutParams(iconSize, iconSize)
                params.gravity = Gravity.BOTTOM or Gravity.START
                params.bottomMargin = 32
                imageView.layoutParams = params
                overlay.addView(imageView)
                icons.add(imageView)

                // Evenly distribute start X along the bottom
                val startX = (screenWidth / (total + 1)) * (index + 1) - iconSize / 2
                imageView.translationX = startX.toFloat()
                imageView.translationY = 0f

                // End X: spread outward from center (salute)
                val center = (total - 1) / 2.0f
                val endX = (screenWidth / 2f) + (index - center) * saluteSpread - iconSize / 2
                val endYAnim = -(startY - endY).toFloat()

                val animatorY = ObjectAnimator.ofFloat(imageView, "translationY", 0f, endYAnim)
                val animatorX = ObjectAnimator.ofFloat(imageView, "translationX", imageView.translationX, endX)
                val fadeOut = ObjectAnimator.ofFloat(imageView, "alpha", 0.7f, 0f)
                fadeOut.startDelay = duration - 600
                fadeOut.duration = 600

                AnimatorSet().apply {
                    playTogether(animatorY, animatorX, fadeOut)
                    setDuration(duration)
                    start()
                }

                handler.postDelayed({
                    overlay.removeView(imageView)
                }, duration)
            }

            var idx = 0
            repeat(bulls) { createIcon(R.drawable.bull, idx++) }
            repeat(cows) { createIcon(R.drawable.cow, idx++) }
        }
    }

    private fun makeGuess(guess: String) {
        val attempt = game.makeGuess(guess)
        if (attempt == null) {
            Toast.makeText(this, getString(R.string.invalid_input), Toast.LENGTH_SHORT).show()
            return
        }

        binding.etGuess.text?.clear()
        updateAttemptsList()

        // Add bulls and cows animation
        animateBullsAndCows(attempt.bulls, attempt.cows)

        if (game.isGameWon()) {
            showWinnerDialog()
            setGameFinishedState()
        }
    }

    private fun updateAttemptsList() {
        val attempts = game.getAttempts()
        attemptsAdapter.updateAttempts(attempts.reversed()) // Show latest attempts at the top
        
        if (attempts.isNotEmpty()) {
            binding.tvNoAttempts.visibility = View.GONE
            binding.rvAttempts.visibility = View.VISIBLE
        } else {
            binding.tvNoAttempts.visibility = View.VISIBLE
            binding.rvAttempts.visibility = View.GONE
        }
    }

    private fun setGameFinishedState() {
        // Disable input field
        binding.etGuess.isEnabled = false
        binding.inputLayout.isEnabled = false
        
        // Change button to "Start New Game"
        binding.btnSuggest.text = getString(R.string.new_game)
        binding.btnSuggest.setOnClickListener {
            startNewGame()
        }
    }

    private fun setGameActiveState() {
        // Enable input field
        binding.etGuess.isEnabled = true
        binding.inputLayout.isEnabled = true
        
        // Restore "Suggest" button
        binding.btnSuggest.text = getString(R.string.suggest)
        binding.btnSuggest.setOnClickListener {
            val guess = binding.etGuess.text.toString()
            if (guess.isNotEmpty()) {
                makeGuess(guess)
            }
        }
    }

    private fun showWinnerDialog() {
        showWinnerDialog = true
        val dialogBinding = DialogWinnerBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        val attempts = game.getAttemptCount()
        val timeInSeconds = game.getTotalGameTime() / 1000

        dialogBinding.tvAttempts.text = getString(R.string.you_won, attempts)
        dialogBinding.tvTime.text = getString(R.string.time_taken, formatTime(timeInSeconds))

        dialogBinding.btnCancel.setOnClickListener {
            showWinnerDialog = false
            dialog.dismiss()
        }

        dialogBinding.btnSave.setOnClickListener {
            val name = dialogBinding.etName.text.toString().trim()
            if (name.isNotEmpty()) {
                saveGameRecord(name, attempts, timeInSeconds)
                showWinnerDialog = false
                dialog.dismiss()
            } else {
                dialogBinding.etName.error = "Please enter your name"
            }
        }

        dialog.show()
    }

    private fun saveGameRecord(name: String, attempts: Int, timeInSeconds: Long) {
        lifecycleScope.launch {
            val record = GameRecord(
                playerName = name,
                attempts = attempts,
                timeInSeconds = timeInSeconds
            )
            database.gameRecordDao().insertRecord(record)
            Toast.makeText(this@MainActivity, "Record saved!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startNewGame() {
        game.startNewGame()
        binding.etGuess.text?.clear()
        updateAttemptsList()
        setGameActiveState()
        binding.etGuess.requestFocus()
    }

    private fun showQuitConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Quit Game")
            .setMessage(getString(R.string.exit_confirmation))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                finish()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun formatTime(seconds: Long): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    private fun showTestWinnerDialog() {
        val dialogBinding = DialogWinnerBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        // Test values
        val attempts = 10
        val timeInSeconds = 60L

        dialogBinding.tvAttempts.text = getString(R.string.you_won, attempts)
        dialogBinding.tvTime.text = getString(R.string.time_taken, formatTime(timeInSeconds))

        // Show happy cows image (already set in XML)
        dialogBinding.ivHappyCows.visibility = View.VISIBLE

        // Disable input field
        dialogBinding.etName.isEnabled = false
        dialogBinding.etName.hint = "(Disabled for test)"
        dialogBinding.btnSave.isEnabled = false

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }
} 