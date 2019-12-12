package aero.testcompany.internetstat.domain.timeline

import aero.testcompany.internetstat.models.NetworkPeriod
import java.util.*

class GetTimeLineMinutesUseCase(private val interval: Long) : GetTimeLine {

    override fun getTimeLine(): List<Long> {
        val timeLine = arrayListOf<Long>()
        val calendar = Calendar.getInstance()
        var startTime = getStartTime(calendar, interval)
        var endTime = getEndTime(calendar, startTime)
        timeLine.add(startTime)
        timeLine.add(endTime)
        for (i in 0 until interval / NetworkPeriod.MINUTES.getStep()) {
            startTime = endTime
            endTime = getEndTime(calendar, startTime)
            timeLine.add(endTime)
        }
        return timeLine
    }

    private fun getStartTime(calendar: Calendar, interval: Long): Long {
        calendar.apply {
            timeInMillis = System.currentTimeMillis() - interval
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    private fun getEndTime(calendar: Calendar, startTime: Long): Long {
        calendar.apply {
            timeInMillis = startTime
            add(Calendar.MINUTE, 1)
        }
        return calendar.timeInMillis
    }
}