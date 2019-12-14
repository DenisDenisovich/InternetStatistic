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

fun Long.isStartOfHour(): Boolean {
    val calend = GregorianCalendar().apply {
        timeInMillis = this@isStartOfHour
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calend.get(Calendar.MINUTE) == 0
}