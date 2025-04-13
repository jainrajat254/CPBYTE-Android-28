package com.example.projecthub.utils

import android.content.Context
import android.os.Build
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object ThemeManager {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_IS_DARK_MODE = "is_dark_mode"
    private const val KEY_THEME_MODE = "theme_mode"

    const val THEME_LIGHT = "Light"
    const val THEME_DARK = "Dark"
    const val THEME_DEFAULT = "Default"

    fun getTheme(context: Context): Flow<Boolean> = flow {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val themeMode = prefs.getString(KEY_THEME_MODE, THEME_LIGHT)

        val isDark = when (themeMode) {
            THEME_DARK -> true
            THEME_LIGHT -> false
            THEME_DEFAULT -> isSystemInDarkTheme(context)
            else -> false
        }

        emit(isDark)
    }

    fun getThemeMode(context: Context): Flow<String> = flow {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val themeMode = prefs.getString(KEY_THEME_MODE, THEME_LIGHT) ?: THEME_LIGHT
        emit(themeMode)
    }

    suspend fun saveTheme(context: Context, isDarkMode: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_IS_DARK_MODE, isDarkMode)
            .putString(KEY_THEME_MODE, if (isDarkMode) THEME_DARK else THEME_LIGHT)
            .apply()
    }

    suspend fun saveThemeMode(context: Context, themeMode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isDarkMode = when (themeMode) {
            THEME_DARK -> true
            THEME_LIGHT -> false
            THEME_DEFAULT -> isSystemInDarkTheme(context)
            else -> false
        }

        prefs.edit()
            .putString(KEY_THEME_MODE, themeMode)
            .putBoolean(KEY_IS_DARK_MODE, isDarkMode)
            .apply()
    }

    private fun isSystemInDarkTheme(context: Context): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                val uiMode = context.resources.configuration.uiMode
                (uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                        android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
            else -> false
        }
    }
}