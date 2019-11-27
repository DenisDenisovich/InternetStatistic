package aero.testcompany.internetstat.models

enum class NetworkInterval {
    ONE_MONTH {
        override fun getInterval(): Long =
            NetworkPeriod.MONTH.getStep()
    },
    TWO_MONTH {
        override fun getInterval(): Long =
            NetworkInterval.ONE_MONTH.getInterval() * 2
    },
    FOUR_MONTH {
        override fun getInterval(): Long =
            NetworkInterval.ONE_MONTH.getInterval() * 4
    },
    SIX_MONTH {
        override fun getInterval(): Long =
            NetworkInterval.ONE_MONTH.getInterval() * 6
    },
    YEAR {
        override fun getInterval(): Long =
            NetworkInterval.ONE_MONTH.getInterval() * 12
    };

    abstract fun getInterval(): Long
}