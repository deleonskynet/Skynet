package com.deleon.skynet.card

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CardManager(private val repository: CardRepository) {
    @JvmOverloads
    fun addCards(
        cards: Array<CardModel>,
        scope: CoroutineScope,
        callback: ((Array<CardModel>) -> Unit)? = null
    ) = scope.launch {
        withContext(Dispatchers.IO) { repository.addCards(*cards) }
        getCards(scope) { if (callback != null) callback(it) }
    }
    @JvmOverloads
    fun deleteCards(
        cards: Array<CardModel>,
        scope: CoroutineScope,
        callback: ((Array<CardModel>) -> Unit)? = null
    ) = scope.launch {
        withContext(Dispatchers.IO) { repository.deleteCards(*cards) }
        withContext(Dispatchers.Main) { getCards(scope) { if (callback != null) callback(it) } }
    }
    @JvmOverloads
    fun editCards(
        profile: String,
        price: Double,
        scope: CoroutineScope,
        callback: ((Array<CardModel>) -> Unit)? = null
    ) = scope.launch {
        withContext(Dispatchers.IO) { repository.editCards(profile, price) }
        withContext(Dispatchers.Main) { getCards(scope) { if (callback != null) callback(it) } }
    }
    fun getCards(
        scope: CoroutineScope,
        callback: (Array<CardModel>) -> Unit
    ) = scope.launch {
        val cards: Array<CardModel> = async(Dispatchers.IO) { repository.getCards() }.await()
        withContext(Dispatchers.Main) { callback(cards) }
    }
}