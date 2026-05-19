package com.ninthbalcony.pushuprpg.utils

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {

    private val DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val TIME_FMT = DateTimeFormatter.ofPattern("HH:mm")

    fun getTodayString(): String = LocalDate.now().format(DATE_FMT)

    fun getTimeString(timestamp: Long): String =
        LocalTime.ofNanoOfDay((timestamp % 86_400_000L) * 1_000_000L).format(TIME_FMT)

    fun getTimeString(): String = LocalTime.now().format(TIME_FMT)

    fun isSameDay(date1: String, date2: String): Boolean = date1 == date2

    fun isYesterday(dateString: String): Boolean =
        LocalDate.now().minusDays(1).format(DATE_FMT) == dateString

    fun getDayOfWeek(dateString: String): String = try {
        val dow = LocalDate.parse(dateString, DATE_FMT).dayOfWeek
        dow.getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH).take(2)
    } catch (e: Exception) { "" }

    fun getDateStringDaysAgo(daysAgo: Int): String =
        LocalDate.now().minusDays(daysAgo.toLong()).format(DATE_FMT)

    fun getDateStringMonthsAgo(monthsAgo: Int): String =
        LocalDate.now().minusMonths(monthsAgo.toLong()).format(DATE_FMT)

    fun getDateStringYearsAgo(yearsAgo: Int): String =
        LocalDate.now().minusYears(yearsAgo.toLong()).format(DATE_FMT)
}
