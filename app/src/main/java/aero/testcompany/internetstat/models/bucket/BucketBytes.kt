package aero.testcompany.internetstat.models.bucket

data class BucketBytes(val received: Long, val transmitted: Long) : ShortString {

    constructor(): this(0L, 0L)

    override fun toString(): String =
        if (received == 0L && transmitted == 0L) {
            ""
        } else {
            "rx: $received, tx: $transmitted"
        }

    override fun toStringShort(): String =
        if (received == 0L && transmitted == 0L) {
            ""
        } else {
            "$received,$transmitted"
        }
}