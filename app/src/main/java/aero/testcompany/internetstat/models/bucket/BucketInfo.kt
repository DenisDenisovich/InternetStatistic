package aero.testcompany.internetstat.models.bucket

data class BucketInfo(
    val all: BucketSource,
    val foreground: BucketSource?,
    val background: BucketSource?
) {
    override fun toString(): String {
        return "all {$all}, foreground {$foreground}, background {$background}"
    }
}