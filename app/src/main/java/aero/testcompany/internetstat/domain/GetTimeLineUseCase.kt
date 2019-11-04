package aero.testcompany.internetstat.domain

import aero.testcompany.internetstat.models.NetworkPeriod
import java.util.*

class GetTimeLineUseCase(private val interval: Long, private val period: NetworkPeriod) {

    fun getTimeLine(): List<Long> {
        val timeLine = arrayListOf<Long>()
        val calendar = Calendar.getInstance()
        var startTime = getStartTime(calendar, interval, period)
        var endTime = getEndTime(calendar, startTime, period)
        timeLine.add(startTime)
        timeLine.add(endTime)
        for (i in 0..interval / period.getStep() + 1) {
            startTime = endTime
            endTime = getEndTime(calendar, startTime, period)
            timeLine.add(endTime)
        }
        return timeLine
    }

    private fun getStartTime(calendar: Calendar, interval: Long, period: NetworkPeriod): Long {
        calendar.apply {
            timeInMillis = System.currentTimeMillis() - interval
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            when (period) {
                NetworkPeriod.MONTH -> set(Calendar.DAY_OF_MONTH, 0)
                NetworkPeriod.WEEK -> set(Calendar.DAY_OF_WEEK, 0)
                NetworkPeriod.DAY -> set(Calendar.HOUR_OF_DAY, 0)
                else -> {
                }
            }
        }
        return calendar.timeInMillis
    }

    private fun getEndTime(calendar: Calendar, startTime: Long, period: NetworkPeriod): Long {
        calendar.apply {
            timeInMillis = startTime
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            when (period) {
                NetworkPeriod.MONTH -> add(Calendar.MONTH, 1)
                NetworkPeriod.WEEK -> add(Calendar.WEEK_OF_YEAR, 1)
                NetworkPeriod.DAY -> add(Calendar.DAY_OF_YEAR, 1)
                else -> {
                }
            }
        }
        return calendar.timeInMillis
    }
}