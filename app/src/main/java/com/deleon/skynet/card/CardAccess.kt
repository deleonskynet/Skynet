package com.deleon.skynet.card

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CardAccess {
    @Insert(CardModel::class, OnConflictStrategy.IGNORE)
    suspend fun addCard(vararg cardModel: CardModel)
    @Delete(CardModel::class)
    suspend fun deleteCard(vararg cardModel: CardModel)
    @Query("UPDATE cards SET price = :price WHERE profile = :profile")
    suspend fun editCard(profile: String, price: Double)
    @Query("SELECT * FROM cards ORDER BY price ASC, profile DESC")
    suspend fun loadCards(): Array<CardModel>
}