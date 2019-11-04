package aero.testcompany.internetstat.models

enum class NetworkPeriod {
    MINUTES{
        override fun getStep() = 1000L * 60L
    },
    HOUR{
        override fun getStep() = MINUTES.getStep() * 60
    },
    DAY{
        override fun getStep() = HOUR.getStep() * 24
    },
    WEEK{
        override fun getStep() = DAY.getStep() * 7
    },
    MONTH{
        override fun getStep() = DAY.getStep() * 31
    };

    abstract fun getStep(): Long
}