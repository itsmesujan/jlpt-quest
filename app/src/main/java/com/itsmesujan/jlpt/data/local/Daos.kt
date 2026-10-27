package com.itsmesujan.jlpt.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM player_profile WHERE id = 1")
    fun observe(): Flow<PlayerProfileEntity?>
    @Query("SELECT * FROM player_profile WHERE id = 1")
    suspend fun get(): PlayerProfileEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: PlayerProfileEntity)
    @Update
    suspend fun update(profile: PlayerProfileEntity)
}

@Dao
interface NodeProgressDao {
    @Query("SELECT * FROM node_progress")
    fun observeAll(): Flow<List<NodeProgressEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: NodeProgressEntity)
}

@Dao
interface ReviewCardDao {
    @Query("SELECT * FROM review_cards ORDER BY dueEpochDay ASC")
    fun observeAll(): Flow<List<ReviewCardEntity>>
    @Query("SELECT * FROM review_cards WHERE dueEpochDay <= :today ORDER BY dueEpochDay ASC")
    fun observeDue(today: Long): Flow<List<ReviewCardEntity>>
    @Query("SELECT COUNT(*) FROM review_cards WHERE dueEpochDay <= :today")
    fun observeDueCount(today: Long): Flow<Int>
    @Query("SELECT * FROM review_cards WHERE itemKey = :itemKey")
    suspend fun get(itemKey: String): ReviewCardEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(card: ReviewCardEntity)
}

@Dao
interface MistakeDao {
    @Query("SELECT * FROM mistakes WHERE cleared = 0 ORDER BY missedEpochDay ASC")
    fun observeOpen(): Flow<List<MistakeEntity>>
    @Insert
    suspend fun insert(mistake: MistakeEntity)
    @Query("UPDATE mistakes SET cleared = 1 WHERE itemKey = :itemKey")
    suspend fun clear(itemKey: String)
}
