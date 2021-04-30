package com.mrntlu.localsocialmedia.service

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

class UserManager(private val context: Context) {
    private val TOKEN_KEY = stringPreferencesKey("token_key")
    private val EMAIL_KEY = stringPreferencesKey("email_key")
    private val USERID_KEY = stringPreferencesKey("userid_key")

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "userToken")

    val userTokenFlow = context.dataStore.data.map {
        it[TOKEN_KEY]
    }

    val userEmailFlow = context.dataStore.data.map {
        it[EMAIL_KEY]
    }

    val userIDFlow = context.dataStore.data.map {
        it[USERID_KEY]
    }

    suspend fun saveToken(token: String, userID: String){
        context.dataStore.edit { settings ->
            settings[TOKEN_KEY] = token
            settings[USERID_KEY] = userID
        }
    }

    suspend fun saveUserInfo(email: String){
        context.dataStore.edit { settings ->
            settings[EMAIL_KEY] = email
        }
    }
}