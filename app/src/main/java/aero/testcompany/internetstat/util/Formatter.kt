package aero.testcompany.internetstat.util

import android.annotation.SuppressLint
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

private var dfMb = DecimalFormat("#.##")
@SuppressLint("ConstantLocale")
private val dfFullDate = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z", Locale.getDefault())

fun toMb(bytes: Long): String = dfMb.format((bytes.toFloat() / 1024) / 1024)

fun Long.getFullDate(): String = dfFullDate.format(this)