package aero.testcompany.internetstat.models.bucket

data class BucketSource(val mobile: BucketBytes, val wifi: BucketBytes) {
    override fun toString(): String {
        return "mobile {$mobile}, wifi {$wifi}"
    }
}