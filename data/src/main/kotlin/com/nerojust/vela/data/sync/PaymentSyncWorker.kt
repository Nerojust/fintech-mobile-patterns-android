package com.nerojust.vela.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nerojust.vela.domain.repository.TransactionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class PaymentSyncWorker
    @AssistedInject
    constructor(
        @Assisted context: Context,
        @Assisted params: WorkerParameters,
        private val transactionRepository: TransactionRepository,
    ) : CoroutineWorker(context, params) {
        // Any unexpected failure from the repository (beyond the per-row handling it already
        // does) should not permanently kill this unique work — retry instead of letting
        // CoroutineWorker's default uncaught-exception handling turn it into Result.failure().
        @Suppress("TooGenericExceptionCaught", "SwallowedException")
        override suspend fun doWork(): Result =
            try {
                val allSynced = transactionRepository.syncPendingTransactions()
                if (allSynced) Result.success() else Result.retry()
            } catch (e: Exception) {
                Result.retry()
            }
    }
