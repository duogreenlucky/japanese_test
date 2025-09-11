package com.duolucky.japanesetest

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface QuestionBaseMenuDao {
    @Insert
    fun add(questionBaseMenu: QuestionBaseMenu)

    @Query("select * from QuestionBaseMenu")
    fun getAll() : List<QuestionBaseMenu>
}