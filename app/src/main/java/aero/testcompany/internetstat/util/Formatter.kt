package aero.testcompany.internetstat.util

import java.text.DecimalFormat

var df = DecimalFormat("#.##")

fun toMb(bytes: Long): String = df.format((bytes.toFloat() / 1024) / 1024)