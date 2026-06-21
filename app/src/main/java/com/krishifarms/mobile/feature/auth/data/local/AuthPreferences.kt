package com.krishifarms.mobile.feature.auth.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.krishifarms.mobile.feature.auth.data.dto.UserDto
import com.krishifarms.mobile.feature.auth.domain.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "krishifarms_auth_preferences",
)

interface AuthPreferences {
    val rememberLogin: Flow<Boolean>
    val cachedUser: Flow<User?>

    suspend fun setRememberLogin(enabled: Boolean)
    suspend fun saveUser(user: User)
    suspend fun clearSessionPreferences()
    suspend fun isRememberLoginEnabled(): Boolean
    suspend fun getCachedUser(): User?
}

@Singleton
class AuthPreferencesImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
) : AuthPreferences {

    private val dataStore = context.authDataStore

    override val rememberLogin: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_REMEMBER_LOGIN] ?: false
    }

    override val cachedUser: Flow<User?> = dataStore.data.map { preferences ->
        preferences[KEY_CACHED_USER]?.let { userJson ->
            runCatching {
                json.decodeFromString<UserDto>(userJson).toDomain()
            }.getOrNull()
        }
    }

    override suspend fun setRememberLogin(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_REMEMBER_LOGIN] = enabled
        }
    }

    override suspend fun saveUser(user: User) {
        val userDto = UserDto(
            id = user.id,
            name = user.name,
            mobile = user.mobile,
            email = user.email,
            role = user.role,
        )
        dataStore.edit { preferences ->
            preferences[KEY_CACHED_USER] = json.encodeToString(userDto)
        }
    }

    override suspend fun clearSessionPreferences() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_CACHED_USER)
        }
    }

    override suspend fun isRememberLoginEnabled(): Boolean =
        dataStore.data.first()[KEY_REMEMBER_LOGIN] ?: false

    override suspend fun getCachedUser(): User? =
        cachedUser.first()

    private companion object {
        val KEY_REMEMBER_LOGIN = booleanPreferencesKey("remember_login")
        val KEY_CACHED_USER = stringPreferencesKey("cached_user")
    }
}

private fun UserDto.toDomain(): User = User(
    id = id,
    name = name,
    mobile = mobile,
    email = email,
    role = role,
)
