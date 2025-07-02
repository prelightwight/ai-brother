package com.prelightwight.aibrother.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state

    fun toggleDarkTheme() {
        _state.value = _state.value.copy(useDarkTheme = !_state.value.useDarkTheme)
    }

    fun toggleTor() {
        _state.value = _state.value.copy(useTor = !_state.value.useTor)
    }
}
