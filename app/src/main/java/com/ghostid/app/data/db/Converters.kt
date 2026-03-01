package com.ghostid.app.data.db

import androidx.room.TypeConverter
import com.ghostid.app.domain.model.AccountStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromAccountStatus(status: AccountStatus): String = status.name

    @TypeConverter
    fun toAccountStatus(value: String): AccountStatus =
        runCatching { AccountStatus.valueOf(value) }.getOrDefault(AccountStatus.PENDING)
}
