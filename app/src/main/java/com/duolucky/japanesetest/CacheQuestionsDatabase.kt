package com.duolucky.japanesetest

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CacheQuestions::class], version = 2)
abstract class CacheQuestionsDatabase : RoomDatabase() {
    abstract fun CacheQuestionsDao() : CacheQuestionsDao
}