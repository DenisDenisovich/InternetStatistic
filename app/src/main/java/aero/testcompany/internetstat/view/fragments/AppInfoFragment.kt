package aero.testcompany.internetstat.view.fragments

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
import android.graphics.Color
import androidx.core.content.ContextCompat
import android.graphics.DashPathEffect
import androidx.lifecycle.ViewModelProviders
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import java.text.SimpleDateFormat
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.*
import kotlin.collections.ArrayList


class AppInfoFragment : Fragment() {

    private val df = SimpleDateFormat("MM.dd", Locale.getDefault())
    private lateinit var networkInfo: NetworkInfo
    private lateinit var myPackageInfo: MyPackageInfo
    private var timeLine = arrayListOf<String>()
    private lateinit var viewModel: AppInfoViewModel
    private val interval = 1000L * 60 * 60 * 24 * 31
    private val period = NetworkPeriod.DAY
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_application_info, container, false)
    }

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
            with(GetTimeLineUseCase(interval, period)) {
                timeLine.clear()
                getTimeLine().forEach { timeStamp ->
                    timeLine.add(df.format(timeStamp))
                }
            }
            fillChart(chart_received, networkInfo.buckets, BytesType.RECEIVED)
            fillChart(chart_transmitted, networkInfo.buckets, BytesType.TRANSMITTED)
            progress.gone()
            group_chart.visible()
        })
        iv_icon.setImageDrawable(
            requireContext().packageManager.getApplicationIcon(myPackageInfo.packageName)
        )
        tv_name.text = myPackageInfo.name
        tv_package.text = myPackageInfo.packageName
        viewModel.update(interval, period)
    }

    private fun fillChart(chart: LineChart, networkData: List<BucketInfo>, bytesType: BytesType) {
        chart.setTouchEnabled(true)
        chart.setPinchZoom(true)
        val lineData = LineData()
        // add all state
        networkData.getNetworkData(NetworkSource.ALL, ApplicationState.ALL, bytesType)
            ?.let { data ->
                lineData.addDataSet(getDataSet(data, NetworkSource.ALL, ApplicationState.ALL))
            }
        networkData.getNetworkData(NetworkSource.MOBILE, ApplicationState.ALL, bytesType)
            ?.let { data ->
                lineData.addDataSet(getDataSet(data, NetworkSource.MOBILE, ApplicationState.ALL))
            }
        networkData.getNetworkData(NetworkSource.WIFI, ApplicationState.ALL, bytesType)
            ?.let { data ->
                lineData.addDataSet(getDataSet(data, NetworkSource.WIFI, ApplicationState.ALL))
            }
        // add foreground state
        networkData.getNetworkData(NetworkSource.ALL, ApplicationState.FOREGROUND, bytesType)
            ?.let { data ->
                lineData.addDataSet(getDataSet(data, NetworkSource.ALL, ApplicationState.FOREGROUND))
            }
        networkData.getNetworkData(NetworkSource.MOBILE, ApplicationState.FOREGROUND, bytesType)
            ?.let { data ->
                lineData.addDataSet(getDataSet(data, NetworkSource.MOBILE, ApplicationState.FOREGROUND))
            }
        networkData.getNetworkData(NetworkSource.WIFI, ApplicationState.FOREGROUND, bytesType)
            ?.let { data ->
                lineData.addDataSet(getDataSet(data, NetworkSource.WIFI, ApplicationState.FOREGROUND))
            }
        // add background state
        networkData.getNetworkData(NetworkSource.ALL, ApplicationState.BACKGROUND, bytesType)
            ?.let { data ->
                lineData.addDataSet(getDataSet(data, NetworkSource.ALL, ApplicationState.BACKGROUND))
            }
        networkData.getNetworkData(NetworkSource.MOBILE, ApplicationState.BACKGROUND, bytesType)
            ?.let { data ->
                lineData.addDataSet(getDataSet(data, NetworkSource.MOBILE, ApplicationState.BACKGROUND))
            }
        networkData.getNetworkData(NetworkSource.WIFI, ApplicationState.BACKGROUND, bytesType)
            ?.let { data ->
                lineData.addDataSet(getDataSet(data, NetworkSource.WIFI, ApplicationState.BACKGROUND))
            }

        chart.apply {
            data = lineData
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

    private fun getDataSet(
        values: List<Long>,
        source: NetworkSource,
        state: ApplicationState
    ): LineDataSet {
        val lineValues: ArrayList<Entry> = arrayListOf()
        for (index in values.indices) {
            lineValues.add(Entry(index.toFloat(), values[index].toMb().toFloat()))
        }
        val graphColor = when(state) {
            ApplicationState.ALL -> R.color.colorAll
            ApplicationState.FOREGROUND -> R.color.colorForeground
            ApplicationState.BACKGROUND -> R.color.colorBackground
        }
        val graphLine = when(source) {
            NetworkSource.ALL -> null
            NetworkSource.WIFI -> Triple(2F, 2F, 0F)
            NetworkSource.MOBILE -> Triple(10F, 5F, 0F)
        }
        val graphDrawable = when(state) {
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
            lineWidth = 1f
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


