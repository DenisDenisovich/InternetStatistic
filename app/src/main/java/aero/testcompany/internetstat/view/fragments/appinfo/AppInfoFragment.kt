package aero.testcompany.internetstat.view.fragments.appinfo

import aero.testcompany.internetstat.R
import aero.testcompany.internetstat.domain.GetTimeLineUseCase
import aero.testcompany.internetstat.models.*
import aero.testcompany.internetstat.models.bucket.BucketInfo
import aero.testcompany.internetstat.util.getNetworkData
import aero.testcompany.internetstat.util.gone
import aero.testcompany.internetstat.util.toMb
import aero.testcompany.internetstat.util.visible
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
    private lateinit var networkInfo: NetworkInfo
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
        viewModel.networkInfo.observe(this, androidx.lifecycle.Observer {
            networkInfo = it
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
            with(GetTimeLineUseCase(interval.getInterval(), period)) {
                timeLine.clear()
                getTimeLine().forEach { timeStamp ->
                    timeLine.add(df.format(timeStamp))
                }
            }
            fillChart(chart_received, networkInfo.buckets, BytesType.RECEIVED)
            fillChart(chart_transmitted, networkInfo.buckets, BytesType.TRANSMITTED)
            progress.gone()
            group_chart.visible()

            changeSelectedLines(BytesType.RECEIVED)
            changeSelectedLines(BytesType.TRANSMITTED)
            networkReceivedLinesData.notifyDataChanged()
            networkTransmittedLinesData.notifyDataChanged()
            chart_transmitted.invalidate()
            chart_received.invalidate()
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
            Toast.makeText(requireContext(), "Minutes doesn't support now", Toast.LENGTH_LONG).show()
            return
        }
        this.period = period
        this.interval = interval
        viewModel.update(interval, period)
        progress.visible()
        group_chart.gone()
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

    private fun fillChart(chart: LineChart, networkData: List<BucketInfo>, bytesType: BytesType) {
        chart.setTouchEnabled(true)
        chart.setPinchZoom(true)
        NetworkSource.values().forEach { source ->
            ApplicationState.values().forEach { state ->
                addDataSet(networkData, source, state, bytesType)
            }
        }
        chart.apply {
            data = getLinesDataSet(bytesType)
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

    private fun getLineDataSet(
        values: List<Long>,
        source: NetworkSource,
        state: ApplicationState
    ): LineDataSet {
        val lineValues: ArrayList<Entry> = arrayListOf()
        for (index in values.indices) {
            lineValues.add(Entry(index.toFloat(), values[index].toMb().toFloat()))
        }
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
        return LineDataSet(lineValues, "").apply {
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

    private fun addDataSet(
        data: List<BucketInfo>,
        source: NetworkSource,
        state: ApplicationState,
        bytesType: BytesType
    ) {
        data.getNetworkData(source, state, bytesType)
            ?.let { networkData ->
                val dataSet = getLineDataSet(networkData, source, state)
                lines.add(
                    NetworkLine(
                        dataSet,
                        source,
                        state,
                        bytesType
                    )
                )
                getLinesDataSet(bytesType).addDataSet(dataSet)
            }
    }

    private fun changeSelectedLines(byteType: BytesType) {
        val sources = if (byteType == BytesType.RECEIVED) sourcesReceived else sourcesTransmitted
        val states = if (byteType == BytesType.RECEIVED) statesReceived else statesTransmitted
        lines.filter { it.bytesType == byteType }
            .forEach { getLinesDataSet(byteType).removeDataSet(it.line) }
        lines.filter {
            it.bytesType == byteType &&
                sources.contains(it.source) &&
                states.contains(it.state)
        }.forEach { getLinesDataSet(byteType).addDataSet(it.line) }
    }

    private fun getLinesDataSet(bytesType: BytesType) = if (bytesType == BytesType.RECEIVED) {
        networkReceivedLinesData
    } else {
        networkTransmittedLinesData
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


