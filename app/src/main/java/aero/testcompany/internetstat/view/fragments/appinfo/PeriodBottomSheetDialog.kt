package aero.testcompany.internetstat.view.fragments.appinfo

import aero.testcompany.internetstat.R
import aero.testcompany.internetstat.models.NetworkInterval
import aero.testcompany.internetstat.models.NetworkPeriod
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_period.*

class PeriodBottomSheetDialog : BottomSheetDialogFragment(), View.OnClickListener {

    private var selectedPeriod = NetworkPeriod.DAY
    private var firstPeriod = NetworkPeriod.DAY
    private var selectedInterval = NetworkInterval.TWO_MONTH
    private var firstInterval = NetworkInterval.TWO_MONTH
    private val periodButtons = arrayListOf<Button>()
    private val intervalButtons = arrayListOf<Button>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_period, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.apply {
            getString(PERIOD_EXTRA)?.let { value ->
                selectedPeriod = NetworkPeriod.valueOf(value)
                firstPeriod = selectedPeriod
            }
            getString(INTERVAL_EXTRA)?.let { value ->
                selectedInterval = NetworkInterval.valueOf(value)
                firstInterval = selectedInterval
            }
        }
        periodButtons.clear()
        periodButtons.addAll(
            arrayListOf(
                btn_period_minutes,
                btn_period_hour,
                btn_period_day,
                btn_period_week,
                btn_period_month
            )
        )
        intervalButtons.clear()
        intervalButtons.addAll(
            arrayListOf(
                btn_interval_1_month,
                btn_interval_2_month,
                btn_interval_4_month,
                btn_interval_6_month,
                btn_interval_year
            )
        )
        periodButtons.forEach {
            it.isSelected = false
            it.setOnClickListener(this)
        }
        intervalButtons.forEach {
            it.isSelected = false
            it.setOnClickListener(this)
        }
        when(selectedPeriod) {
            NetworkPeriod.MINUTES -> btn_period_minutes.isSelected = true
            NetworkPeriod.HOUR -> btn_period_hour.isSelected = true
            NetworkPeriod.DAY -> btn_period_day.isSelected = true
            NetworkPeriod.WEEK -> btn_period_week.isSelected = true
            NetworkPeriod.MONTH -> btn_period_month.isSelected = true
        }
        when(selectedInterval) {
            NetworkInterval.ONE_MONTH -> btn_interval_1_month.isSelected = true
            NetworkInterval.TWO_MONTH -> btn_interval_2_month.isSelected = true
            NetworkInterval.FOUR_MONTH -> btn_interval_4_month.isSelected = true
            NetworkInterval.SIX_MONTH -> btn_interval_6_month.isSelected = true
            NetworkInterval.YEAR -> btn_interval_year.isSelected = true
        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_period_minutes -> changePeriod(NetworkPeriod.MINUTES, btn_period_minutes)
            R.id.btn_period_hour -> changePeriod(NetworkPeriod.HOUR, btn_period_hour)
            R.id.btn_period_day -> changePeriod(NetworkPeriod.DAY, btn_period_day)
            R.id.btn_period_week -> changePeriod(NetworkPeriod.WEEK, btn_period_week)
            R.id.btn_period_month -> changePeriod(NetworkPeriod.MONTH, btn_period_month)
            R.id.btn_interval_1_month -> changeInterval(NetworkInterval.ONE_MONTH, btn_interval_1_month)
            R.id.btn_interval_2_month -> changeInterval(NetworkInterval.TWO_MONTH, btn_interval_2_month)
            R.id.btn_interval_4_month -> changeInterval(NetworkInterval.FOUR_MONTH, btn_interval_4_month)
            R.id.btn_interval_6_month -> changeInterval(NetworkInterval.SIX_MONTH, btn_interval_6_month)
            R.id.btn_interval_year -> changeInterval(NetworkInterval.YEAR, btn_interval_year)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (firstPeriod != selectedPeriod || firstInterval != selectedInterval) {
            listener?.changePeriodAndInterval(selectedPeriod, selectedInterval)
        }
        listener = null
    }

    private fun changePeriod(period: NetworkPeriod, btn: Button) {
        periodButtons.forEach { it.isSelected = false }
        if (selectedPeriod != period) {
            selectedPeriod = period
            btn.isSelected = true
        }
    }

    private fun changeInterval(interval: NetworkInterval, btn: Button) {
        intervalButtons.forEach { it.isSelected = false }
        if (selectedInterval != interval) {
            selectedInterval = interval
            btn.isSelected = true
        }
    }

    interface PeriodListener {
        fun changePeriodAndInterval(period: NetworkPeriod, interval: NetworkInterval)
    }

    companion object {
        var listener: PeriodListener? = null
        private const val PERIOD_EXTRA = "period"
        private const val INTERVAL_EXTRA = "interval"

        fun getInstance(
            listener: PeriodListener,
            period: NetworkPeriod,
            interval: NetworkInterval
        ) = PeriodBottomSheetDialog().apply {
            arguments = Bundle().apply {
                this@Companion.listener = listener
                putString(PERIOD_EXTRA, period.name)
                putString(INTERVAL_EXTRA, interval.name)
            }
        }
    }
}