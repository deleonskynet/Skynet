package com.deleon.skynet.card

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity("cards", primaryKeys = ["username"])
data class CardModel(
    @ColumnInfo("username") val username: String,
    @ColumnInfo("password") val password: String = "",
    @ColumnInfo("profile") val profile: String = "default",
    @ColumnInfo("time_limit") val timeLimit: String = "",
    @ColumnInfo("data_limit") val dataLimit: String = "",
    @ColumnInfo("comment") val comment: String = "",
    @ColumnInfo("price") val price: Double = 0.0
)