package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SchoolBellConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolConfigDao {
    @Query("SELECT * FROM school_config WHERE id = 1 LIMIT 1")
    fun getConfig(): Flow<SchoolBellConfig?>

    @Query("SELECT * FROM school_config WHERE id = 1 LIMIT 1")
    suspend fun getConfigDirect(): SchoolBellConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConfig(config: SchoolBellConfig)

    @Update
    suspend fun updateConfig(config: SchoolBellConfig)
}
