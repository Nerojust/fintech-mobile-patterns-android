package com.nerojust.vela.core.security

import org.junit.Assert.assertEquals
import org.junit.Test

class RedactingTreeTest {
    @Test
    fun `redacts a 12 to 19 digit run as a card number`() {
        val message = "Charging card 4242424242424242 now"
        assertEquals("Charging card [REDACTED_CARD] now", RedactingTree.redact(message))
    }

    @Test
    fun `redacts amount fields`() {
        val message = "amount=15000 submitted"
        assertEquals("amount=[REDACTED] submitted", RedactingTree.redact(message))
    }

    @Test
    fun `redacts token fields case-insensitively`() {
        val message = "AuthToken: abc123XYZ sent"
        assertEquals("AuthToken=[REDACTED] sent", RedactingTree.redact(message))
    }

    @Test
    fun `leaves unrelated text untouched`() {
        val message = "Navigated to SendMoneyScreen"
        assertEquals(message, RedactingTree.redact(message))
    }

    @Test
    fun `redacts a space-separated card number`() {
        val message = "Charging card 4242 4242 4242 4242 now"
        assertEquals("Charging card [REDACTED_CARD] now", RedactingTree.redact(message))
    }

    @Test
    fun `redacts a dash-separated card number`() {
        val message = "Charging card 4242-4242-4242-4242 now"
        assertEquals("Charging card [REDACTED_CARD] now", RedactingTree.redact(message))
    }

    @Test
    fun `does not redact small unrelated number sequences`() {
        val message = "I have 1000 dollars and won 999 points in 2024"
        assertEquals(message, RedactingTree.redact(message))
    }
}
