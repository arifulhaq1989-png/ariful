package com.example.core

import java.security.MessageDigest
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

object Formatters {
    fun formatMoney(amount: Double, currencySymbol: String): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = if (abs(amount % 1.0) > 0.005) 2 else 0
            maximumFractionDigits = 2
        }
        val sign = if (amount < -0.005) "-" else ""
        return "$sign$currencySymbol${formatter.format(abs(amount))}"
    }

    fun formatDate(timestamp: Long, language: AppLanguage): String {
        val locale = if (language == AppLanguage.BN) Locale("bn", "BD") else Locale.ENGLISH
        return SimpleDateFormat("dd MMM yyyy", locale).format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long, language: AppLanguage): String {
        val locale = if (language == AppLanguage.BN) Locale("bn", "BD") else Locale.ENGLISH
        return SimpleDateFormat("dd MMM yyyy, hh:mm a", locale).format(Date(timestamp))
    }

    fun formatShortDay(timestamp: Long, language: AppLanguage): String {
        val locale = if (language == AppLanguage.BN) Locale("bn", "BD") else Locale.ENGLISH
        return SimpleDateFormat("EEE", locale).format(Date(timestamp))
    }

    fun initials(name: String): String {
        val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> "BZ"
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> "${parts[0].first()}${parts[1].first()}".uppercase()
        }
    }
}

object SecurityHasher {
    fun sha256(input: String): String {
        val salted = "BizSphere_Vault_2026:$input"
        val digest = MessageDigest.getInstance("SHA-256").digest(salted.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
