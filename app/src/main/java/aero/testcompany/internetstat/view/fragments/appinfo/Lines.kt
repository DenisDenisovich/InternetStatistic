package aero.testcompany.internetstat.view.fragments.appinfo

import aero.testcompany.internetstat.models.ApplicationState
import aero.testcompany.internetstat.models.BytesType
import aero.testcompany.internetstat.models.NetworkSource
import aero.testcompany.internetstat.models.bucket.BucketInfo
import aero.testcompany.internetstat.util.getNetworkData
import aero.testcompany.internetstat.util.toMb
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
    private val df = SimpleDateFormat("MM.dd", Locale.getDefault())
    val timeLine = arrayListOf<String>()

    fun setTimeLine(longTimeLine: List<Long>) {
        timeLine.clear()
        timeLine.addAll(longTimeLine.map { df.format(it) })
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
                        Entry((lastIndex - index).toFloat(), bytes.toMb().toFloat())
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