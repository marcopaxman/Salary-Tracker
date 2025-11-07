package com.example.waiterwallet

import com.example.waiterwallet.data.DailyEntry
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DailyEntryTest {
    @Test
    fun totalTips_sumsCashAndCard() {
        val e = DailyEntry(
            date = LocalDate.of(2025, 1, 1),
            turnover = 0.0,
            tipsCash = 10.0,
            tipsCard = 5.5,
            notes = null
        )
        assertEquals(15.5, e.totalTips, 0.0001)
    }

    @Test
    fun totalTips_handlesNulls() {
        val e = DailyEntry(
            date = LocalDate.of(2025, 1, 2),
            turnover = 0.0,
            tipsCash = null,
            tipsCard = null,
            notes = null
        )
        assertEquals(0.0, e.totalTips, 0.0001)
    }
}
