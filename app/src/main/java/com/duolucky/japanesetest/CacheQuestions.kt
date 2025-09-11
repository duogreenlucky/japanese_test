package com.duolucky.japanesetest

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class CacheQuestions(
//    @ColumnInfo(name = "dateAt")
    var question: String,
    var answer: String,
    ) {
    @PrimaryKey(autoGenerate = true)
    var amountid: Long = 0
}