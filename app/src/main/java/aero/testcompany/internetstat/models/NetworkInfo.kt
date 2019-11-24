package aero.testcompany.internetstat.models

import aero.testcompany.internetstat.models.bucket.BucketInfo
import java.io.Serializable

data class NetworkInfo(
    val packageInfo: MyPackageInfo,
    val buckets: List<BucketInfo>,
    val timeLine: List<Long>
) : Serializable