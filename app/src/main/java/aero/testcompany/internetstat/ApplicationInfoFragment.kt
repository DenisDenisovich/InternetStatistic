package aero.testcompany.internetstat

import aero.testcompany.internetstat.models.MyPackageInfo
import aero.testcompany.internetstat.models.NetworkInfo
import aero.testcompany.internetstat.util.PackageManagerHelper
import aero.testcompany.internetstat.util.PackageNetworkInfo
import aero.testcompany.internetstat.util.toMb
import android.app.usage.NetworkStatsManager
import android.content.Context
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
import com.github.mikephil.charting.components.XAxis
import java.text.SimpleDateFormat
import com.github.mikephil.charting.formatter.ValueFormatter


class ApplicationInfoFragment : Fragment() {

    private val df = SimpleDateFormat("MM.dd")
    private lateinit var networkInfo: NetworkInfo
    private var timeLine = arrayListOf<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_application_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        networkInfo = getNetworkInfo()
        createTimeLine()
        var totalReceived: Long = 0
        var totalTransmitted: Long = 0
        for (i in 0 until networkInfo.received.size) {
            totalReceived += networkInfo.received[i]
            totalTransmitted += networkInfo.transmitted[i]
        }
        tv_received.text = "Received " + toMb(totalReceived) + "Mb"
        tv_transmitted.text = "Transmitted " + toMb(totalTransmitted) + "Mb"

        iv_icon.setImageDrawable(context!!.packageManager.getApplicationIcon(networkInfo.packageName))
        tv_name.text = networkInfo.label
        tv_package.text = networkInfo.packageName
        fillReceivedChart()
        fillTransmittedChart()
    }

    private fun getNetworkInfo(): NetworkInfo {
        val hour = 1000L * 60 * 60 * 24 * 31
        val step = 1000L * 60 * 60 * 24
        val info = arguments!!.getSerializable(INFO_KEY) as MyPackageInfo
        val networkStatsManager =
            activity!!.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        val uid = PackageManagerHelper.getPackageUid(context!!, info.packageName)
        val networkHelper = PackageNetworkInfo(context!!, networkStatsManager, uid)
        val (received, transmitted) = networkHelper.getInfo(hour, step)
        return NetworkInfo(
            info.packageName,
            info.name,
            received,
            transmitted,
            hour,
            step
        )
    }

    private fun fillReceivedChart() {
        chart_received.setTouchEnabled(true)
        chart_received.setPinchZoom(true)
        val values = arrayListOf<Entry>()
        for (i in 0 until networkInfo.received.size) {
            values.add(Entry(i.toFloat(), toMb(networkInfo.received[i]).toFloat()))
        }

        val receivedDataSet: LineDataSet
        receivedDataSet = LineDataSet(values, "")
        receivedDataSet.apply {
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
            val drawable = ContextCompat.getDrawable(context!!, R.drawable.fade_blue)
            fillDrawable = drawable
            isHighlightEnabled = false
        }
        val lineData = LineData()
        lineData.addDataSet(receivedDataSet)
        chart_received.apply {
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

    private fun fillTransmittedChart() {
        chart_transmitted.setTouchEnabled(true)
        chart_transmitted.setPinchZoom(true)
        val values = arrayListOf<Entry>()
        for (i in 0 until networkInfo.transmitted.size) {
            values.add(Entry(i.toFloat(), toMb(networkInfo.transmitted[i]).toFloat()))
        }

        val transmittedDataSet: LineDataSet
        transmittedDataSet = LineDataSet(values, "")
        transmittedDataSet.apply {
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
            val drawable = ContextCompat.getDrawable(context!!, R.drawable.fade_blue)
            fillDrawable = drawable
            isHighlightEnabled = false
        }
        val lineData = LineData()
        lineData.addDataSet(transmittedDataSet)
        chart_transmitted.apply {
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

    private fun createTimeLine() {
        val current = System.currentTimeMillis()
        val count = (current - networkInfo.interval) / networkInfo.step
        var time = current - networkInfo.interval
        for (i in 0 until count) {
            timeLine.add(df.format(time))
            time += networkInfo.step
        }
    }

    companion object {
        private val INFO_KEY = "key"
        fun getInstance(networkInfo: MyPackageInfo): ApplicationInfoFragment =
            ApplicationInfoFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(INFO_KEY, networkInfo)
                }
            }
    }
}


