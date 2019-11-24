package aero.testcompany.internetstat.models

import android.app.usage.NetworkStats

enum class ApplicationState(val value: Int) {
    ALL(NetworkStats.Bucket.STATE_ALL),
    FOREGROUND(NetworkStats.Bucket.STATE_FOREGROUND),
    BACKGROUND(NetworkStats.Bucket.STATE_DEFAULT)
}