package aero.testcompany.internetstat.models.bucket

data class BucketInfo(
    val all: BucketSource,
    val foreground: BucketSource?,
    val background: BucketSource?
) : ShortString {

    constructor(): this(BucketSource(), BucketSource(), BucketSource())

    override fun toString(): String =
        if (all.toString().isEmpty() &&
            foreground?.toString().isNullOrEmpty() &&
            background?.toString().isNullOrEmpty()
        ) {
            ""
        } else {
            "all {$all}, foreground {$foreground}, background {$background}"
        }

    override fun toStringShort(): String =
        if (all.toStringShort().isEmpty() &&
            foreground?.toStringShort().isNullOrEmpty() &&
            background?.toStringShort().isNullOrEmpty()
        ) {
            ""
        } else {
            "{${all.toStringShort()}},{${foreground?.toStringShort()}},{${background?.toStringShort()}}"
        }
}