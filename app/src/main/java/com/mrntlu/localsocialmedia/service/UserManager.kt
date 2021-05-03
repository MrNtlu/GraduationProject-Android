package com.mrntlu.localsocialmedia.service

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "userToken")

class UserManager(private val context: Context) {
    private val TOKEN_KEY = stringPreferencesKey("token_key")
    private val USERID_KEY = stringPreferencesKey("userid_key")

    fun getUserInfo() = userTokenFlow.combine(userIDFlow){ token, id->
        Pair(token, id)
    }

    private val userTokenFlow = context.dataStore.data.map {
        it[TOKEN_KEY]
    }

    private val userIDFlow = context.dataStore.data.map {
        it[USERID_KEY]
    }

    suspend fun saveToken(token: String, userID: String){
        context.dataStore.edit { settings ->
            settings[TOKEN_KEY] = token
            settings[USERID_KEY] = userID
        }
    }

    suspend fun deleteDataStore(){
        context.dataStore.edit{
            it.clear()
        }
    }
}