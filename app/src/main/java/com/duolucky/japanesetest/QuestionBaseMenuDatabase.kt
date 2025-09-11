package com.duolucky.japanesetest

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [QuestionBaseMenu::class], version = 1)
abstract class QuestionBaseMenuDatabase : RoomDatabase() {
    abstract fun questionBaseMenuDao() : QuestionBaseMenuDao
}