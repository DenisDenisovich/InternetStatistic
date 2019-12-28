package aero.testcompany.internetstat.view.fragments.appinfo

import aero.testcompany.internetstat.R
import aero.testcompany.internetstat.models.*
import aero.testcompany.internetstat.util.*
import aero.testcompany.internetstat.viewmodel.AppInfoViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.fragment_application_info.*
import com.github.mikephil.charting.data.LineData
import androidx.core.content.ContextCompat
import android.graphics.DashPathEffect
import android.util.Log
import androidx.lifecycle.ViewModelProviders
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlin.collections.ArrayList

class AppInfoFragment : Fragment(),
    View.OnClickListener,
    LinesBottomSheetDialog.OnGraphSelected,
    PeriodBottomSheetDialog.PeriodListener {

    private lateinit var myPackageInfo: MyPackageInfo
    private lateinit var viewModel: AppInfoViewModel
    private var interval = NetworkInterval.TWO_MONTH
    private var period = NetworkPeriod.DAY
    private var lines = NetworkLinesList()
    private var networkReceivedLinesData = LineData()
    private var networkTransmittedLinesData = LineData()
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
            tv_received.text = resources.getString(R.string.total_received_app, it.toMb())
        })
        viewModel.totalTransmitted.observe(this, androidx.lifecycle.Observer {
            tv_transmitted.text = resources.getString(R.string.total_transmitted_app, it.toMb())
        })
        viewModel.timeLine.observe(this, androidx.lifecycle.Observer {
            lines.setTimeLine(it, period)
        })
        viewModel.networkInfo.observe(this, androidx.lifecycle.Observer { buckets ->
            if (progress.isVisible()) {
                progress.gone()
            }
            if (group_chart.isGone()) {
                NetworkSource.values().forEach { source ->
                    ApplicationState.values().forEach { state ->
                        BytesType.values().forEach { type ->
                            val dataSet = initLineDataSet(source, state)
                            lines.add(NetworkLine(dataSet, source, state, type))
                            getLineData(type).addDataSet(dataSet)
                        }
                    }
                }
                changeLinesVisibility(BytesType.RECEIVED)
                changeLinesVisibility(BytesType.TRANSMITTED)
                initChart(BytesType.RECEIVED)
                initChart(BytesType.TRANSMITTED)
                group_chart.visible()
            }
            NetworkSource.values().forEach { source ->
                ApplicationState.values().forEach { state ->
                    BytesType.values().forEach { type ->
                        lines.updateDataSet(buckets, source, state, type)
                    }
                }
            }
            updateGraph()
        })

        iv_icon.setImageDrawable(
            requireContext().packageManager.getApplicationIcon(myPackageInfo.packageName)
        )
        tv_name.text = myPackageInfo.name
        tv_package.text = myPackageInfo.packageName
        viewModel.update(interval, period)

        btn_received_lines.setOnClickListener(this)
        btn_transmitted_lines.setOnClickListener(this)
        btn_period.setOnClickListener(this)


        chart_received.setOnTouchListener { _, _ ->
            updateGraphYRange(chart_received, getLineData(BytesType.RECEIVED))
            return@setOnTouchListener false
        }
        chart_transmitted.setOnTouchListener { _, _ ->
            updateGraphYRange(chart_transmitted, getLineData(BytesType.TRANSMITTED))
            return@setOnTouchListener false
        }
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
        changeLinesVisibility(bytesType)
        updateGraph()
    }

    override fun onStateSelected(bytesType: BytesType, states: ArrayList<ApplicationState>) {
        if (bytesType == BytesType.RECEIVED) {
            statesReceived.clear()
            statesReceived.addAll(states)
        } else {
            statesTransmitted.clear()
            statesTransmitted.addAll(states)
        }
        changeLinesVisibility(bytesType)
        updateGraph()
    }

    private fun initChart(bytesType: BytesType) {
        val chart = if (bytesType == BytesType.RECEIVED) chart_received else chart_transmitted
        chart.apply {
            fitScreen()
            setTouchEnabled(true)
            setPinchZoom(true)
            data = getLineData(bytesType)
            axisRight.labelCount = 6
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val result = lines.timeLine.getOrNull(value.toInt()) ?: "error"
                        Log.d(
                            "logFormatter",
                            "size: ${lines.timeLine.size}, value: $value, result: $result"
                        )
                        return result
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

    private fun changeLinesVisibility(byteType: BytesType) {
        val sources = if (byteType == BytesType.RECEIVED) sourcesReceived else sourcesTransmitted
        val states = if (byteType == BytesType.RECEIVED) statesReceived else statesTransmitted
        // remove all lines from chart
        lines.filter(byteType).forEach { getLineData(byteType).removeDataSet(it.line) }
        // add only selected lines
        lines.filter(sources, states, byteType)
            .forEach { getLineData(byteType).addDataSet(it.line) }
    }

    private fun getLineData(bytesType: BytesType) = if (bytesType == BytesType.RECEIVED) {
        networkReceivedLinesData
    } else {
        networkTransmittedLinesData
    }

    private fun clearGraphs() {
        lines = NetworkLinesList()
        networkReceivedLinesData = LineData()
        networkTransmittedLinesData = LineData()
        changeLinesVisibility(BytesType.RECEIVED)
        changeLinesVisibility(BytesType.TRANSMITTED)
        chart_received.apply {
            data = LineData()
            data?.notifyDataChanged()
            xAxis?.valueFormatter = null
            notifyDataSetChanged()
            invalidate()
        }
        chart_transmitted.apply {
            data = LineData()
            data?.notifyDataChanged()
            xAxis?.valueFormatter = null
            notifyDataSetChanged()
            invalidate()
        }
        networkReceivedLinesData.notifyDataChanged()
        networkTransmittedLinesData.notifyDataChanged()
        progress.visible()
        group_chart.gone()
    }

    private fun updateGraph() {
        networkReceivedLinesData.notifyDataChanged()
        networkTransmittedLinesData.notifyDataChanged()
        chart_transmitted?.apply {
            notifyDataSetChanged()
            invalidate()
        }
        chart_received?.apply {
            notifyDataSetChanged()
            invalidate()
        }
    }

    private fun updateGraphYRange(graph: LineChart, lineData: LineData) {
        val lowestX = graph.lowestVisibleX
        val highestX = graph.highestVisibleX
        lineData.calcMinMaxY(lowestX, highestX)
        val minVisibleY = lineData.yMin
        val maxVisibleY = lineData.yMax
        val topOffset = maxVisibleY / 6
        if (maxVisibleY != 0F && minVisibleY >= 0) {
            graph.setVisibleYRange(minVisibleY, maxVisibleY + topOffset, YAxis.AxisDependency.RIGHT)
        }
    }

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