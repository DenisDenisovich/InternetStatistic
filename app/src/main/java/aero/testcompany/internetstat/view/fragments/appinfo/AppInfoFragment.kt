package aero.testcompany.internetstat.view.fragments.appinfo

import aero.testcompany.internetstat.R
import aero.testcompany.internetstat.models.*
import aero.testcompany.internetstat.models.bucket.BucketInfo
import aero.testcompany.internetstat.util.*
import aero.testcompany.internetstat.viewmodel.AppInfoViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.fragment_application_info.*
import com.github.mikephil.charting.data.LineData
import androidx.core.content.ContextCompat
import android.graphics.DashPathEffect
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import java.text.SimpleDateFormat
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.*
import kotlin.collections.ArrayList

class AppInfoFragment : Fragment(),
    View.OnClickListener,
    LinesBottomSheetDialog.OnGraphSelected,
    PeriodBottomSheetDialog.PeriodListener {

    private val df = SimpleDateFormat("MM.dd", Locale.getDefault())
    private lateinit var myPackageInfo: MyPackageInfo
    private var timeLine = arrayListOf<String>()
    private lateinit var viewModel: AppInfoViewModel
    private var interval = NetworkInterval.TWO_MONTH
    private var period = NetworkPeriod.DAY
    private val lines = arrayListOf<NetworkLine>()
    private val networkReceivedLinesData = LineData()
    private val networkTransmittedLinesData = LineData()
    private val sourcesTransmitted = arrayListOf(NetworkSource.ALL)
    private val statesTransmitted = arrayListOf(ApplicationState.ALL)
    private val sourcesReceived = arrayListOf(NetworkSource.ALL)
    private val statesReceived = arrayListOf(ApplicationState.ALL)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_application_info, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        myPackageInfo = arguments?.getParcelable(INFO_KEY) as MyPackageInfo
        viewModel = ViewModelProviders.of(this)[AppInfoViewModel::class.java]
        viewModel.initData(requireContext(), myPackageInfo)
        viewModel.totalReceived.observe(this, androidx.lifecycle.Observer {
            tv_received.text = resources.getString(R.string.total_received, it.toMb())
        })
        viewModel.totalTransmitted.observe(this, androidx.lifecycle.Observer {
            tv_transmitted.text = resources.getString(R.string.total_transmitted, it.toMb())
        })
        viewModel.timeLine.observe(this, androidx.lifecycle.Observer {
            timeLine.clear()
            it.forEach { timeStamp ->
                timeLine.add(df.format(timeStamp))
            }
        })
        viewModel.networkInfo.observe(this, androidx.lifecycle.Observer { buckets ->
            if (progress.isVisible()) {
                progress.gone()
            }
            if (group_chart.isGone()) {
                initChart(BytesType.RECEIVED)
                initChart(BytesType.TRANSMITTED)
                group_chart.visible()
            }
            NetworkSource.values().forEach { source ->
                ApplicationState.values().forEach { state ->
                    BytesType.values().forEach { type ->
                        updateDataSet(buckets, source, state, type)
                    }
                }
            }
            networkReceivedLinesData.notifyDataChanged()
            networkTransmittedLinesData.notifyDataChanged()
            chart_transmitted.invalidate()
            chart_received.invalidate()
        })
        // init lines
        NetworkSource.values().forEach { source ->
            ApplicationState.values().forEach { state ->
                BytesType.values().forEach { type ->
                    val dataSet = initLineDataSet(source, state)
                    lines.add(NetworkLine(dataSet, source, state, type))
                    getTypedDataSet(type).addDataSet(dataSet)
                }
            }
        }
        changeSelectedLines(BytesType.RECEIVED)
        changeSelectedLines(BytesType.TRANSMITTED)

        iv_icon.setImageDrawable(
            requireContext().packageManager.getApplicationIcon(myPackageInfo.packageName)
        )
        tv_name.text = myPackageInfo.name
        tv_package.text = myPackageInfo.packageName
        viewModel.update(interval, period)

        btn_received_lines.setOnClickListener(this)
        btn_transmitted_lines.setOnClickListener(this)
        btn_period.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_received_lines -> {
                LinesBottomSheetDialog
                    .getInstance(this, BytesType.RECEIVED, sourcesReceived, statesReceived)
                    .show(childFragmentManager, null)
            }
            R.id.btn_transmitted_lines -> {
                LinesBottomSheetDialog
                    .getInstance(this, BytesType.TRANSMITTED, sourcesTransmitted, statesTransmitted)
                    .show(childFragmentManager, null)
            }
            R.id.btn_period ->
                PeriodBottomSheetDialog
                    .getInstance(this, period, interval)
                    .show(childFragmentManager, null)
        }
    }

    override fun changePeriodAndInterval(period: NetworkPeriod, interval: NetworkInterval) {
        if (period == NetworkPeriod.MINUTES) {
            Toast.makeText(
                requireContext(),
                "Minutes doesn't support now", Toast.LENGTH_LONG
            ).show()
            return
        }
        this.period = period
        this.interval = interval
        clearGraphs()
        progress.visible()
        group_chart.gone()
        viewModel.update(interval, period)
    }

    override fun onSourceSelected(bytesType: BytesType, sources: ArrayList<NetworkSource>) {
        if (bytesType == BytesType.RECEIVED) {
            sourcesReceived.clear()
            sourcesReceived.addAll(sources)
        } else {
            sourcesTransmitted.clear()
            sourcesTransmitted.addAll(sources)
        }
        changeSelectedLines(bytesType)
        networkReceivedLinesData.notifyDataChanged()
        networkTransmittedLinesData.notifyDataChanged()
        chart_transmitted.invalidate()
        chart_received.invalidate()
    }

    override fun onStateSelected(bytesType: BytesType, states: ArrayList<ApplicationState>) {
        if (bytesType == BytesType.RECEIVED) {
            statesReceived.clear()
            statesReceived.addAll(states)
        } else {
            statesTransmitted.clear()
            statesTransmitted.addAll(states)
        }
        changeSelectedLines(bytesType)
        networkReceivedLinesData.notifyDataChanged()
        networkTransmittedLinesData.notifyDataChanged()
        chart_received.invalidate()
        chart_transmitted.invalidate()
    }

    private fun initChart(bytesType: BytesType) {
        val chart = if (bytesType == BytesType.RECEIVED) chart_received else chart_transmitted
        chart.apply {
            setTouchEnabled(true)
            setPinchZoom(true)
            data = getTypedDataSet(bytesType)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return timeLine[value.toInt()]
                    }
                }
                granularity = 1f
            }
            description.isEnabled = false
            legend.isEnabled = false
        }
    }

    private fun initLineDataSet(
        source: NetworkSource,
        state: ApplicationState
    ): LineDataSet {
        val graphColor = when (state) {
            ApplicationState.ALL -> R.color.colorAll
            ApplicationState.FOREGROUND -> R.color.colorForeground
            ApplicationState.BACKGROUND -> R.color.colorBackground
        }
        val graphLine = when (source) {
            NetworkSource.ALL -> null
            NetworkSource.MOBILE -> null
            NetworkSource.WIFI -> Triple(15F, 15F, 0F)
        }
        val dataLineWidth = when (source) {
            NetworkSource.ALL -> 2f
            else -> 1f
        }
        val graphDrawable = when (state) {
            ApplicationState.ALL -> R.drawable.fade_all
            ApplicationState.FOREGROUND -> R.drawable.fade_foreground
            ApplicationState.BACKGROUND -> R.drawable.fade_background
        }
        return LineDataSet(mutableListOf(), "").apply {
            setDrawIcons(false)
            graphLine?.let {
                enableDashedLine(it.first, it.second, it.third)
                enableDashedHighlightLine(it.first, it.second, it.third)
            }
            setDrawValues(false)
            setDrawCircles(false)
            color = graphColor
            lineWidth = dataLineWidth
            circleRadius = 3f
            setDrawCircleHole(false)
            valueTextSize = 9f
            setDrawFilled(true)
            formLineWidth = 1f
            formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
            formSize = 15f
            val drawable = ContextCompat.getDrawable(
                requireContext(),
                graphDrawable
            )
            fillDrawable = drawable
            isHighlightEnabled = false
        }
    }

    private fun updateDataSet(
        data: List<BucketInfo>,
        source: NetworkSource,
        state: ApplicationState,
        bytesType: BytesType
    ) {
        data.getNetworkData(source, state, bytesType)?.let { networkData ->
            lines.filter { it.source == source && it.state == state && it.bytesType == bytesType }
                .getOrNull(0)?.line
                ?.let { currentDataSet ->
                    for (index in networkData.indices) {
                        currentDataSet.addEntry(
                            Entry(index.toFloat(), networkData[index].toMb().toFloat())
                        )
                    }
                }
        }
    }

    private fun changeSelectedLines(byteType: BytesType) {
        val sources = if (byteType == BytesType.RECEIVED) sourcesReceived else sourcesTransmitted
        val states = if (byteType == BytesType.RECEIVED) statesReceived else statesTransmitted
        lines.filter { it.bytesType == byteType }
            .forEach { getTypedDataSet(byteType).removeDataSet(it.line) }
        lines.filter {
            it.bytesType == byteType &&
                sources.contains(it.source) &&
                states.contains(it.state)
        }.forEach { getTypedDataSet(byteType).addDataSet(it.line) }
    }

    private fun getTypedDataSet(bytesType: BytesType) = if (bytesType == BytesType.RECEIVED) {
        networkReceivedLinesData
    } else {
        networkTransmittedLinesData
    }

    private fun clearGraphs() {
        lines.clear()
        networkReceivedLinesData.clearValues()
        networkTransmittedLinesData.clearValues()
        chart_received.apply {
            fitScreen()
            data?.clearValues()
            xAxis?.valueFormatter = null
            notifyDataSetChanged()
            clear()
            invalidate()
        }
        chart_transmitted.apply {
            fitScreen()
            data?.clearValues()
            xAxis?.valueFormatter = null
            notifyDataSetChanged()
            clear()
            invalidate()
        }
        progress.visible()
        group_chart.gone()
    }

    class NetworkLine(
        val line: LineDataSet,
        val source: NetworkSource,
        val state: ApplicationState,
        val bytesType: BytesType
    )

    companion object {
        private const val INFO_KEY = "key"
        fun getInstance(packageInfo: MyPackageInfo): AppInfoFragment =
            AppInfoFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(INFO_KEY, packageInfo)
                }
            }
    }
}