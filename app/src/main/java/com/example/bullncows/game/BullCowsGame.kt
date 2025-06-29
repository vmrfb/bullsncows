package com.example.bullncows.game

import com.example.bullncows.model.Attempt
import kotlin.random.Random

class BullCowsGame {
    private var secretNumber: String = ""
    private val attempts = mutableListOf<Attempt>()
    private var gameStartTime: Long = 0
    private var isGameActive = false

    fun startNewGame() {
        secretNumber = generateSecretNumber()
        attempts.clear()
        gameStartTime = System.currentTimeMillis()
        isGameActive = true
    }

    private fun generateSecretNumber(): String {
        val digits = mutableListOf<Int>()
        while (digits.size < 4) {
            val digit = Random.nextInt(0, 10)
            if (digits.isEmpty() && digit == 0) continue // No leading zero
            if (!digits.contains(digit)) {
                digits.add(digit)
            }
        }
        return digits.joinToString("")
    }

    fun makeGuess(guess: String): Attempt? {
        if (!isValidGuess(guess)) {
            return null
        }

        val bulls = countBulls(guess)
        val cows = countCows(guess)

        val attempt = Attempt(guess, bulls, cows)
        attempts.add(attempt)

        if (bulls == 4) {
            isGameActive = false
        }

        return attempt
    }

    private fun isValidGuess(guess: String): Boolean {
        if (guess.length != 4) return false
        if (guess[0] == '0') return false // No leading zero

        val digits = guess.toSet()
        if (digits.size != 4) return false // All digits must be different

        return guess.all { it.isDigit() }
    }

    private fun countBulls(guess: String): Int {
        var bulls = 0
        for (i in guess.indices) {
            if (guess[i] == secretNumber[i]) {
                bulls++
            }
        }
        return bulls
    }

    private fun countCows(guess: String): Int {
        var cows = 0
        val secretDigits = secretNumber.toSet()
        val guessDigits = guess.toSet()

        for (digit in guessDigits) {
            if (secretDigits.contains(digit)) {
                cows++
            }
        }

        // Subtract bulls from cows
        cows -= countBulls(guess)
        return cows
    }

    fun getAttempts(): List<Attempt> = attempts.toList()

    fun isGameWon(): Boolean = attempts.isNotEmpty() && attempts.last().bulls == 4

    fun isGameActive(): Boolean = isGameActive

    fun getGameTime(): Long {
        return if (isGameActive) {
            System.currentTimeMillis() - gameStartTime
        } else {
            0
        }
    }

    fun getTotalGameTime(): Long {
        return System.currentTimeMillis() - gameStartTime
    }

    fun getAttemptCount(): Int = attempts.size

    fun getGameState(): String {
        return "$secretNumber|${attempts.joinToString(",") { "${it.guess}:${it.bulls}:${it.cows}" }}|$gameStartTime|$isGameActive"
    }

    fun restoreFromState(state: String) {
        val parts = state.split("|")
        if (parts.size >= 4) {
            secretNumber = parts[0]
            val attemptsStr = parts[1]
            gameStartTime = parts[2].toLongOrNull() ?: System.currentTimeMillis()
            isGameActive = parts[3].toBoolean()
            
            attempts.clear()
            if (attemptsStr.isNotEmpty()) {
                attemptsStr.split(",").forEach { attemptStr ->
                    val attemptParts = attemptStr.split(":")
                    if (attemptParts.size == 3) {
                        val guess = attemptParts[0]
                        val bulls = attemptParts[1].toInt()
                        val cows = attemptParts[2].toInt()
                        attempts.add(Attempt(guess, bulls, cows))
                    }
                }
            }
        }
    }
}