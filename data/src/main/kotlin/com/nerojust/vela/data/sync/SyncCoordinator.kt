package com.nerojust.vela.data.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncCoordinator
    @Inject
    constructor(
        private val connectivityObserver: ConnectivityObserver,
        private val syncScheduler: SyncScheduler,
    ) {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        /** Call once from [com.nerojust.vela.VelaApplication.onCreate]. */
        fun start() {
            scope.launch {
                connectivityObserver.observe()
                    .filter { isConnected -> isConnected }
                    .collect { syncScheduler.scheduleSync() }
            }
        }
    }
