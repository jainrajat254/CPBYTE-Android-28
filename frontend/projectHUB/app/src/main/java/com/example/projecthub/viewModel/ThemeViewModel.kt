package com.example.projecthub.viewModel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projecthub.utils.ThemeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val _themeMode = MutableStateFlow(ThemeManager.THEME_LIGHT)
    val themeMode: StateFlow<String> = _themeMode
        .stateIn(viewModelScope, SharingStarted.Lazily, ThemeManager.THEME_LIGHT)

    private val uiModeListener = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_CONFIGURATION_CHANGED) {
                if (_themeMode.value == ThemeManager.THEME_DEFAULT) {
                    updateThemeFromSystem()
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            ThemeManager.getTheme(application).collect { savedTheme ->
                _isDarkMode.emit(savedTheme)
            }

            ThemeManager.getThemeMode(application).collect { savedMode ->
                _themeMode.emit(savedMode)
            }
        }

        // Register for system theme changes
        val filter = IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED)
        application.registerReceiver(uiModeListener, filter)
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unregisterReceiver(uiModeListener)
    }

    private fun updateThemeFromSystem() {
        val nightModeFlags = getApplication<Application>().resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        val systemIsDark = nightModeFlags == Configuration.UI_MODE_NIGHT_YES
        viewModelScope.launch {
            _isDarkMode.emit(systemIsDark)
        }
    }

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            ThemeManager.saveTheme(getApplication(), isDark)
            _isDarkMode.emit(isDark)
            _themeMode.emit(if (isDark) ThemeManager.THEME_DARK else ThemeManager.THEME_LIGHT)
        }
    }

    fun setThemeMode(themeMode: String) {
        viewModelScope.launch {
            ThemeManager.saveThemeMode(getApplication(), themeMode)
            _themeMode.emit(themeMode)

            // Update dark mode based on new theme setting
            when (themeMode) {
                ThemeManager.THEME_LIGHT -> _isDarkMode.emit(false)
                ThemeManager.THEME_DARK -> _isDarkMode.emit(true)
                ThemeManager.THEME_DEFAULT -> updateThemeFromSystem()
            }
        }
    }
}