package io.github.nwma_fywf.mineword.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM words ORDER BY createdAt DESC")
    fun getAllWords(): Flow<List<Word>>

    @Query("SELECT * FROM words ORDER BY word ASC")
    fun getAllWordsSortedByName(): Flow<List<Word>>

    @Query("SELECT * FROM words ORDER BY nextReviewTime ASC")
    fun getAllWordsSortedByReviewTime(): Flow<List<Word>>

    @Query("SELECT * FROM words ORDER BY learningStage DESC")
    fun getAllWordsSortedByMastery(): Flow<List<Word>>

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getWordById(id: Long): Word?

    @Query("""
        SELECT DISTINCT w.* FROM words w
        LEFT JOIN meanings m ON w.id = m.wordId
        WHERE w.word LIKE '%' || :query || '%'
           OR m.definition LIKE '%' || :query || '%'
           OR w.tags LIKE '%' || :query || '%'
        ORDER BY w.createdAt DESC
    """)
    fun searchWords(query: String): Flow<List<Word>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: Word): Long

    @Update
    suspend fun updateWord(word: Word)

    @Delete
    suspend fun deleteWord(word: Word)

    @Query("SELECT tags FROM words WHERE tags != ''")
    fun getAllTagsRaw(): Flow<List<String>>

    @Query("SELECT * FROM words WHERE LOWER(word) = LOWER(:word) LIMIT 1")
    suspend fun getWordByWord(word: String): Word?

    @Query("SELECT * FROM words ORDER BY createdAt DESC")
    suspend fun getAllWordsOnce(): List<Word>

    @Query("SELECT * FROM words WHERE nextReviewTime <= :currentTime AND nextReviewTime > 0 ORDER BY nextReviewTime ASC")
    suspend fun getWordsDueForReview(currentTime: Long): List<Word>

    @Query("SELECT COUNT(*) FROM words WHERE nextReviewTime <= :currentTime AND nextReviewTime > 0")
    suspend fun getDueReviewCount(currentTime: Long): Int

    @Query("SELECT COUNT(*) FROM words")
    suspend fun getWordCount(): Int

    @Query("SELECT COUNT(*) FROM words")
    fun getWordCountFlow(): Flow<Int>

    @Query("DELETE FROM words")
    suspend fun deleteAllWords()

    @Query("SELECT * FROM words WHERE tags LIKE '%' || :tag || '%' ORDER BY createdAt DESC")
    fun getWordsByTag(tag: String): Flow<List<Word>>

    @Query("SELECT * FROM words WHERE tags LIKE '%' || :tag || '%' ORDER BY createdAt DESC")
    suspend fun getWordsByTagOnce(tag: String): List<Word>

    @Query("SELECT * FROM words WHERE (',' || REPLACE(tags, ' ', '') || ',') LIKE '%,' || REPLACE(:tag1, ' ', '') || ',%' AND (',' || REPLACE(tags, ' ', '') || ',') LIKE '%,' || REPLACE(:tag2, ' ', '') || ',%' ORDER BY createdAt DESC")
    suspend fun getWordsByTwoTagsOnce(tag1: String, tag2: String): List<Word>
}
