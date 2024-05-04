package com.deleon.skynet.card

import androidx.annotation.WorkerThread

/**
 * Card Repository.
 *
 * @constructor Create [CardRepository]
 * @property cardAccess [CardAccess] interface of data access object
 */
class CardRepository(private val cardAccess: CardAccess) {
    /**
     * Add Cards.
     *
     * @param cardModel [CardModel]s to be added
     */
    @WorkerThread
    suspend fun addCards(vararg cardModel: CardModel) {
        cardAccess.addCard(*cardModel)
    }

    /**
     * Delete Cards.
     *
     * @param cardModel [CardModel]s to be removed
     */
    @WorkerThread
    suspend fun deleteCards(vararg cardModel: CardModel) {
        cardAccess.deleteCard(*cardModel)
    }

    /**
     * Edit Cards.
     *
     * @param profile [String] profile of [CardModel]s to be updated
     * @param price [Double] new price amounts of updated [CardModel]s
     */
    @WorkerThread
    suspend fun editCards(profile: String, price: Double) {
        cardAccess.editCard(profile, price)
    }

    /**
     * Get Cards.
     *
     * @return [Array] of [CardModel] if given profile is not null or empty,
     * result filtered for [CardModel]s that match with given profile only
     */
    @WorkerThread
    suspend fun getCards(): Array<CardModel> {
        return cardAccess.loadCards()
    }
}