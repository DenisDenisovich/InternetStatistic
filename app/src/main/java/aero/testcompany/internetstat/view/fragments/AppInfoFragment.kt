package aero.testcompany.internetstat.view.fragments

import aero.testcompany.internetstat.R
import aero.testcompany.internetstat.models.NetworkInfo
import aero.testcompany.internetstat.domain.GetTimeLineUseCase
import aero.testcompany.internetstat.models.MyPackageInfo
import aero.testcompany.internetstat.models.NetworkPeriod
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
            fillChart(chart_received, networkInfo.received)
            fillChart(chart_transmitted, networkInfo.transmitted)
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

    private fun fillChart(chart: LineChart, networkData: List<Long>) {
        chart.setTouchEnabled(true)
        chart.setPinchZoom(true)
        val values = arrayListOf<Entry>()
        for (i in networkData.indices) {
            values.add(Entry(i.toFloat(), networkData[i].toMb().toFloat()))
        }

        val dataSet: LineDataSet
        dataSet = LineDataSet(values, "")
        dataSet.apply {
            setDrawIcons(false)
            enableDashedLine(10f, 5f, 0f)
            enableDashedHighlightLine(10f, 5f, 0f)
            setDrawValues(false)
            setDrawCircles(false)
            color = Color.DKGRAY
            setCircleColor(Color.DKGRAY)
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
                R.drawable.fade_blue
            )
            fillDrawable = drawable
            isHighlightEnabled = false
        }
        val lineData = LineData()
        lineData.addDataSet(dataSet)
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


