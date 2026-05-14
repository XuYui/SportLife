package com.sportlife.records.data.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserPreferencesRepository(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val _homeSlogan = MutableStateFlow(preferences.getString(KEY_HOME_SLOGAN, DEFAULT_HOME_SLOGAN) ?: DEFAULT_HOME_SLOGAN)

    val homeSlogan: StateFlow<String> = _homeSlogan

    fun saveHomeSlogan(value: String) {
        val slogan = value.trim().ifBlank { DEFAULT_HOME_SLOGAN }
        preferences.edit().putString(KEY_HOME_SLOGAN, slogan).apply()
        _homeSlogan.value = slogan
    }

    fun currentHomeSlogan(): String = _homeSlogan.value

    companion object {
        const val PREFERENCES_NAME = "sport_life_preferences"
        private const val KEY_HOME_SLOGAN = "home_slogan"
        const val DEFAULT_HOME_SLOGAN = "日拱一卒，功不唐捐。"
    }
}
