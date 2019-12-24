package aero.testcompany.internetstat.view.fragments.appinfo

import aero.testcompany.internetstat.models.ApplicationState
import aero.testcompany.internetstat.models.BytesType
import aero.testcompany.internetstat.models.NetworkPeriod
import aero.testcompany.internetstat.models.NetworkSource
import aero.testcompany.internetstat.models.bucket.BucketInfo
import aero.testcompany.internetstat.util.getNetworkData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class NetworkLine(
    val line: LineDataSet,
    val source: NetworkSource,
    val state: ApplicationState,
    val bytesType: BytesType
)

class NetworkLinesList : ArrayList<NetworkLine>() {
    private val dfMonth = SimpleDateFormat("MM.dd", Locale.getDefault())
    private val dfHour = SimpleDateFormat("HH-MM.dd", Locale.getDefault())
    private val dfMinutes = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeLine = arrayListOf<String>()

    fun setTimeLine(longTimeLine: List<Long>, period: NetworkPeriod) {
        val currentFormatter = when (period) {
            NetworkPeriod.HOUR -> dfHour
            NetworkPeriod.MINUTES -> dfMinutes
            else -> dfMonth
        }
        timeLine.clear()
        timeLine.addAll(longTimeLine.map { currentFormatter.format(it) })
    }

    fun updateDataSet(
        data: List<BucketInfo>,
        source: NetworkSource,
        state: ApplicationState,
        bytesType: BytesType
    ) {
        val networkData = data.getNetworkData(source, state, bytesType)
        val currentDataSet = filter(source, state, bytesType).getOrNull(0)?.line
        networkData?.let {
            currentDataSet?.let {
                val lastIndex = timeLine.lastIndex - currentDataSet.values.size
                networkData.forEachIndexed { index, bytes ->
                    currentDataSet.addEntryOrdered(
                        Entry((lastIndex - index).toFloat(), (bytes.toFloat() / 1024) / 1024)
                    )
                }
                currentDataSet.notifyDataSetChanged()
            }
        }
    }

    fun filter(
        sources: ArrayList<NetworkSource>,
        states: ArrayList<ApplicationState>,
        bytesType: BytesType
    ) = filter {
        it.bytesType == bytesType &&
                sources.contains(it.source) &&
                states.contains(it.state)
    }

    fun filter(
        source: NetworkSource,
        state: ApplicationState,
        bytesType: BytesType
    ) = filter { it.source == source && it.state == state && it.bytesType == bytesType }

    fun filter(bytesType: BytesType) = filter { it.bytesType == bytesType }
}