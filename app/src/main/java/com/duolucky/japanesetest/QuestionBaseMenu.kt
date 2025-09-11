package com.duolucky.japanesetest

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class QuestionBaseMenu(
    var name: String,
    var id: String
) {
    @PrimaryKey(autoGenerate = true)
    var amountID: Long = 0
}