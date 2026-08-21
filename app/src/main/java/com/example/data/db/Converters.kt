package com.example.data.db

import androidx.room.TypeConverter
import com.example.data.model.BreakConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val breakListType = Types.newParameterizedType(List::class.java, BreakConfig::class.java)
    private val breakListAdapter = moshi.adapter<List<BreakConfig>>(breakListType)

    private val intListType = Types.newParameterizedType(List::class.java, Integer::class.java)
    private val intListAdapter = moshi.adapter<List<Int>>(intListType)

    @TypeConverter
    fun fromBreakList(breaks: List<BreakConfig>?): String {
        return if (breaks == null) "[]" else breakListAdapter.toJson(breaks)
    }

    @TypeConverter
    fun toBreakList(json: String?): List<BreakConfig> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            breakListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromIntList(list: List<Int>?): String {
        return if (list == null) "[]" else intListAdapter.toJson(list)
    }

    @TypeConverter
    fun toIntList(json: String?): List<Int> {
        if (json.isNullOrEmpty()) return listOf(1, 2, 3, 4, 5)
        return try {
            intListAdapter.fromJson(json) ?: listOf(1, 2, 3, 4, 5)
        } catch (e: Exception) {
            listOf(1, 2, 3, 4, 5)
        }
    }
}
