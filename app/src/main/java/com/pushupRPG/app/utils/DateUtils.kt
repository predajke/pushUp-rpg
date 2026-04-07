package com.pushupRPG.app.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun getTodayString(): String {
        return dateFormat.format(Date())
    }

    fun getTimeString(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    fun getTimeString(): String {
        return timeFormat.format(Date())
    }

    fun isSameDay(date1: String, date2: String): Boolean {
        return date1 == date2
    }

    fun isYesterday(dateString: String): Boolean {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return dateFormat.format(calendar.time) == dateString
    }

    fun getDayOfWeek(dateString: String): String {
        return try {
            val date = dateFormat.parse(dateString) ?: return ""
            val cal = Calendar.getInstance()
            cal.time = date
            when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "Mo"
                Calendar.TUESDAY -> "Tu"
                Calendar.WEDNESDAY -> "We"
                Calendar.THURSDAY -> "Th"
                Calendar.FRIDAY -> "Fr"
                Calendar.SATURDAY -> "Sa"
                Calendar.SUNDAY -> "Su"
                else -> ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun getDateStringDaysAgo(daysAgo: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return dateFormat.format(calendar.time)
    }

    fun getDateStringMonthsAgo(monthsAgo: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -monthsAgo)
        return dateFormat.format(calendar.time)
    }

    fun getDateStringYearsAgo(yearsAgo: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -yearsAgo)
        return dateFormat.format(calendar.time)
    }
}