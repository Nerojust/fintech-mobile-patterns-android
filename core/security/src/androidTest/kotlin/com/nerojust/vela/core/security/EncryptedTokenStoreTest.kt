package com.nerojust.vela.core.security

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EncryptedTokenStoreTest {
    private val store = EncryptedTokenStore(ApplicationProvider.getApplicationContext())

    @Test
    fun saveAndGetRoundTripsTheToken() {
        store.saveAuthToken("token")
        assertEquals("token", store.getAuthToken())
    }

    @Test
    fun clearRemovesTheSavedToken() {
        store.saveAuthToken("token")
        store.clear()
        assertNull(store.getAuthToken())
    }
}
