package com.deleon.skynet

import android.app.Application
import com.deleon.skynet.card.CardDatabase
import com.deleon.skynet.card.CardRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob

@Suppress("MemberVisibilityCanBePrivate")
class SkynetApp : Application() {
    val appJob = SupervisorJob()
    val doScope = CoroutineScope(Dispatchers.IO + Job(appJob))
    val database by lazy { CardDatabase.getDatabase(this) }
    val repository by lazy { CardRepository(database.cardAccess()) }
    fun close() {
        database.close()
        appJob.cancel()
    }
}