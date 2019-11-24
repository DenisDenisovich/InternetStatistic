package aero.testcompany.internetstat.models.bucket

data class BucketBytes(val received: Long, val transmitted: Long) {
    override fun toString(): String {
        return "rx: $received, tx: $transmitted"
    }
}