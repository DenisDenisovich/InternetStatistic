package aero.testcompany.internetstat.models.bucket

data class BucketSource(val mobile: BucketBytes, val wifi: BucketBytes) : ShortString {
    override fun toString(): String =
        if (mobile.toString().isEmpty() && wifi.toString().isEmpty()) {
            ""
        } else {
            "mobile {$mobile}, wifi {$wifi}"
        }

    override fun toStringShort(): String =
        if (mobile.toStringShort().isEmpty() && wifi.toStringShort().isEmpty()) {
            ""
        } else {
            "{${mobile.toStringShort()}}{${wifi.toStringShort()}}"
        }
}