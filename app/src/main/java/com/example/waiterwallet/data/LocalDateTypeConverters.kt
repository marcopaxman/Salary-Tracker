package com.example.waiterwallet.data

import androidx.room.TypeConverter
import java.time.LocalDate

class LocalDateTypeConverters {
    @TypeConverter
    fun fromEpochDay(epochDay: Long?): LocalDate? = epochDay?.let { LocalDate.ofEpochDay(it) }

    @TypeConverter
    fun toEpochDay(date: LocalDate?): Long? = date?.toEpochDay()
}
