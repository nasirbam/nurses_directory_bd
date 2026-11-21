package com.example.data

import java.security.MessageDigest
import java.security.SecureRandom

object SecurityUtils {
    private val secureRandom = SecureRandom()

    fun generateSalt(): String {
        val bytes = ByteArray(16)
        secureRandom.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hashPin(pin: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val input = "$salt:$pin".toByteArray(Charsets.UTF_8)
        val hashBytes = digest.digest(input)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(pin: String, salt: String, expectedHash: String): Boolean {
        if (pin.isBlank() || salt.isBlank() || expectedHash.isBlank()) return false
        val actual = hashPin(pin, salt)
        return MessageDigest.isEqual(actual.toByteArray(), expectedHash.toByteArray())
    }

    fun isValidBangladeshPhone(phone: String): Boolean {
        val cleaned = phone.replace(Regex("[\\s\\-]"), "")
        return cleaned.matches(Regex("^(\\+?88)?01[3-9]\\d{8}$"))
    }

    fun formatWhatsAppDigits(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        return when {
            digits.startsWith("8801") && digits.length == 13 -> digits
            digits.startsWith("01") && digits.length == 11 -> "88$digits"
            else -> digits
        }
    }

    fun isValidEmail(email: String): Boolean {
        return email.trim().matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
    }
}
