package com.example.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.core.AppLanguage
import com.example.core.SecurityHasher
import com.example.core.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bizsphere_prefs")

data class UserPreferences(
    val language: AppLanguage = AppLanguage.EN,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val currencySymbol: String = "৳",
    val businessName: String = "BizSphere Store",
    val ownerName: String = "",
    val businessPhone: String = "",
    val businessAddress: String = "",
    val accountEmail: String = "",
    val accountPasswordHash: String = "",
    val isRegistered: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isPinEnabled: Boolean = false,
    val pinHash: String = "",
    val lastBackupAt: Long = 0L
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val CURRENCY = stringPreferencesKey("currency_symbol")
        val BUSINESS_NAME = stringPreferencesKey("business_name")
        val OWNER_NAME = stringPreferencesKey("owner_name")
        val BUSINESS_PHONE = stringPreferencesKey("business_phone")
        val BUSINESS_ADDRESS = stringPreferencesKey("business_address")
        val ACCOUNT_EMAIL = stringPreferencesKey("account_email")
        val ACCOUNT_PASSWORD_HASH = stringPreferencesKey("account_password_hash")
        val IS_REGISTERED = booleanPreferencesKey("is_registered")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val IS_PIN_ENABLED = booleanPreferencesKey("is_pin_enabled")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val LAST_BACKUP_AT = longPreferencesKey("last_backup_at")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            language = AppLanguage.fromCode(prefs[Keys.LANGUAGE] ?: AppLanguage.EN.code),
            themeMode = ThemeMode.fromCode(prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.code),
            currencySymbol = prefs[Keys.CURRENCY] ?: "৳",
            businessName = prefs[Keys.BUSINESS_NAME] ?: "BizSphere Store",
            ownerName = prefs[Keys.OWNER_NAME] ?: "",
            businessPhone = prefs[Keys.BUSINESS_PHONE] ?: "",
            businessAddress = prefs[Keys.BUSINESS_ADDRESS] ?: "",
            accountEmail = prefs[Keys.ACCOUNT_EMAIL] ?: "",
            accountPasswordHash = prefs[Keys.ACCOUNT_PASSWORD_HASH] ?: "",
            isRegistered = prefs[Keys.IS_REGISTERED] ?: false,
            isLoggedIn = prefs[Keys.IS_LOGGED_IN] ?: false,
            isPinEnabled = prefs[Keys.IS_PIN_ENABLED] ?: false,
            pinHash = prefs[Keys.PIN_HASH] ?: "",
            lastBackupAt = prefs[Keys.LAST_BACKUP_AT] ?: 0L
        )
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.code }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.code }
    }

    suspend fun setCurrencySymbol(symbol: String) {
        context.dataStore.edit { it[Keys.CURRENCY] = symbol }
    }

    suspend fun updateBusinessProfile(
        businessName: String,
        ownerName: String,
        phone: String,
        address: String
    ) {
        context.dataStore.edit {
            it[Keys.BUSINESS_NAME] = businessName.trim().ifBlank { "BizSphere Store" }
            it[Keys.OWNER_NAME] = ownerName.trim()
            it[Keys.BUSINESS_PHONE] = phone.trim()
            it[Keys.BUSINESS_ADDRESS] = address.trim()
        }
    }

    suspend fun registerAccount(
        ownerName: String,
        businessName: String,
        emailOrPhone: String,
        password: String
    ) {
        val hash = SecurityHasher.sha256(password)
        context.dataStore.edit {
            it[Keys.OWNER_NAME] = ownerName.trim()
            it[Keys.BUSINESS_NAME] = businessName.trim().ifBlank { "BizSphere Store" }
            it[Keys.ACCOUNT_EMAIL] = emailOrPhone.trim()
            it[Keys.ACCOUNT_PASSWORD_HASH] = hash
            it[Keys.IS_REGISTERED] = true
            it[Keys.IS_LOGGED_IN] = true
        }
    }

    suspend fun continueQuickStart() {
        context.dataStore.edit {
            it[Keys.IS_LOGGED_IN] = true
        }
    }

    suspend fun setLoggedIn(loggedIn: Boolean) {
        context.dataStore.edit {
            it[Keys.IS_LOGGED_IN] = loggedIn
        }
    }

    suspend fun setPin(pin: String) {
        val hash = SecurityHasher.sha256(pin)
        context.dataStore.edit {
            it[Keys.PIN_HASH] = hash
            it[Keys.IS_PIN_ENABLED] = true
        }
    }

    suspend fun disablePin() {
        context.dataStore.edit {
            it[Keys.IS_PIN_ENABLED] = false
            it[Keys.PIN_HASH] = ""
        }
    }

    suspend fun setLastBackupAt(timestamp: Long) {
        context.dataStore.edit {
            it[Keys.LAST_BACKUP_AT] = timestamp
        }
    }
}
