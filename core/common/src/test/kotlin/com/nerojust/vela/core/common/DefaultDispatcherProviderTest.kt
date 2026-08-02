package com.nerojust.vela.core.common

import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultDispatcherProviderTest {
    @Test
    fun `provides real dispatchers`() {
        val provider: DispatcherProvider = DefaultDispatcherProvider()
        assertEquals(Dispatchers.IO, provider.io)
        assertEquals(Dispatchers.Main, provider.main)
        assertEquals(Dispatchers.Default, provider.default)
    }
}
