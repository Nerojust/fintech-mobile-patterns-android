package com.nerojust.vela.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MoneyTest {
    @Test
    fun `plus adds minor units for the same currency`() {
        val total = Money(1_000, Currency.USD) + Money(250, Currency.USD)
        assertEquals(1_250, total.minorUnits)
    }

    @Test
    fun `plus throws for mismatched currencies`() {
        assertThrows(IllegalArgumentException::class.java) {
            Money(1_000, Currency.USD) + Money(250, Currency.EUR)
        }
    }

    @Test
    fun `formatted renders two decimal digits`() {
        assertEquals("USD 12.50", Money(1_250, Currency.USD).formatted())
    }

    @Test
    fun `formatted pads a single minor digit`() {
        assertEquals("USD 12.05", Money(1_205, Currency.USD).formatted())
    }
}
