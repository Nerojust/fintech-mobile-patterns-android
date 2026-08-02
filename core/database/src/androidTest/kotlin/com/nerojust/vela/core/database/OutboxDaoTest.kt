package com.nerojust.vela.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OutboxDaoTest {
    private lateinit var db: VelaDatabase
    private lateinit var dao: OutboxDao

    private fun entity(
        id: String,
        status: String,
    ) = OutboxTransactionEntity(
        id = id,
        amountMinorUnits = 1_000,
        currency = "USD",
        destinationCurrency = "EUR",
        cardId = "card-1",
        status = status,
        failureReason = null,
        attempts = 0,
        createdAtEpochMillis = 1L,
    )

    @Before
    fun setUp() {
        db =
            Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), VelaDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        dao = db.outboxDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndObserveReturnsInsertedRow() =
        runTest {
            dao.insert(entity("txn-1", "PENDING"))
            val rows = dao.observeAll().first()
            assertEquals(1, rows.size)
            assertEquals("PENDING", rows.first().status)
        }

    @Test
    fun getPendingExcludesSyncedRows() =
        runTest {
            dao.insert(entity("txn-1", "PENDING"))
            dao.insert(entity("txn-2", "SYNCED"))
            val pending = dao.getPending()
            assertEquals(1, pending.size)
            assertEquals("txn-1", pending.first().id)
        }

    @Test
    fun updateChangesStatus() =
        runTest {
            dao.insert(entity("txn-1", "PENDING"))
            dao.update(entity("txn-1", "SYNCED"))
            assertEquals(0, dao.getPending().size)
        }
}
