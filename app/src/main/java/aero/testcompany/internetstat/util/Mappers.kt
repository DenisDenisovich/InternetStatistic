package aero.testcompany.internetstat.util

import aero.testcompany.internetstat.models.ApplicationState
import aero.testcompany.internetstat.models.BytesType
import aero.testcompany.internetstat.models.NetworkSource
import aero.testcompany.internetstat.models.bucket.BucketBytes
import aero.testcompany.internetstat.models.bucket.BucketInfo
import aero.testcompany.internetstat.models.bucket.BucketSource

fun BucketInfo.getState(state: ApplicationState): BucketSource? = when (state) {
    ApplicationState.ALL -> all
    ApplicationState.FOREGROUND -> foreground
    ApplicationState.BACKGROUND -> background
}

fun BucketSource.getSource(source: NetworkSource): BucketBytes = when (source) {
    NetworkSource.WIFI -> wifi
    NetworkSource.MOBILE -> mobile
    NetworkSource.ALL -> BucketBytes(
        mobile.received + wifi.received,
        mobile.transmitted + wifi.transmitted
    )
}

fun BucketBytes.getType(type: BytesType) = when (type) {
    BytesType.RECEIVED -> received
    BytesType.TRANSMITTED -> transmitted
}

fun List<BucketInfo>.getNetworkData(
    source: NetworkSource,
    state: ApplicationState,
    bytesType: BytesType
): List<Long>? =
    if (getOrNull(0)?.getState(state) == null) {
        null
    } else {
        map { it.getState(state)!!.getSource(source).getType(bytesType) }
    }

operator fun BucketInfo.minus(value: BucketInfo): BucketInfo =
    BucketInfo(
        all = BucketSource(
            mobile = BucketBytes(
                all.mobile.received - value.all.mobile.received,
                all.mobile.transmitted - value.all.mobile.transmitted
            ),
            wifi = BucketBytes(
                all.wifi.received - value.all.wifi.received,
                all.wifi.transmitted - value.all.wifi.transmitted
            )
        ),
        foreground = BucketSource(
            mobile = BucketBytes(
                (foreground?.mobile?.received ?: 0) - (value.foreground?.mobile?.received ?: 0),
                (foreground?.mobile?.transmitted ?: 0) - (value.foreground?.mobile?.transmitted ?: 0)
            ),
            wifi = BucketBytes(
                (foreground?.wifi?.received ?: 0) - (value.foreground?.wifi?.received ?: 0),
                (foreground?.wifi?.transmitted ?: 0) - (value.foreground?.wifi?.transmitted ?: 0)
            )
        ),
        background = BucketSource(
            mobile = BucketBytes(
                (foreground?.mobile?.received ?: 0) - (value.foreground?.mobile?.received ?: 0),
                (foreground?.mobile?.transmitted ?: 0) - (value.foreground?.mobile?.transmitted ?: 0)
            ),
            wifi = BucketBytes(
                (background?.wifi?.received ?: 0) - (value.background?.wifi?.received ?: 0),
                (background?.wifi?.transmitted ?: 0) - (value.background?.wifi?.transmitted ?: 0)
            )
        )
    )
