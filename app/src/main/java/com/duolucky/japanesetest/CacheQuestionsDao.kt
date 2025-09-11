package com.duolucky.japanesetest

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CacheQuestionsDao {
    @Insert
    fun add(questions: ArrayList<CacheQuestions>)

    @Query("select * from CacheQuestions")
    fun getAll() : List<CacheQuestions>

    @Delete
    fun delete(questions: CacheQuestions)
}