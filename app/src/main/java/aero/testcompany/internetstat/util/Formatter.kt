package aero.testcompany.internetstat.util

import android.annotation.SuppressLint
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

private var dfMb = DecimalFormat("#.##")
@SuppressLint("ConstantLocale")
private val dfFullDate = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z", Locale.getDefault())

fun Long.toMb(): String = dfMb.format((this.toFloat() / 1024) / 1024)

fun Long.getFullDate(): String = dfFullDate.format(this)

fun Long.isSameHour(anotherTime: Long): Boolean {
    val firstData = GregorianCalendar().apply {
        timeInMillis = this@isSameHour
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val secondData = GregorianCalendar().apply {
        timeInMillis = anotherTime
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return firstData.timeInMillis == secondData.timeInMillis
}