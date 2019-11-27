package aero.testcompany.internetstat.view.fragments.appinfo

import aero.testcompany.internetstat.R
import aero.testcompany.internetstat.models.ApplicationState
import aero.testcompany.internetstat.models.BytesType
import aero.testcompany.internetstat.models.NetworkSource
import aero.testcompany.internetstat.util.color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_lines.*

class LinesBottomSheetDialog : BottomSheetDialogFragment(), View.OnClickListener {

    private var selectedStates = arrayListOf<ApplicationState>()
    private var selectedSources = arrayListOf<NetworkSource>()
    private var type: BytesType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            getString(KEY_TYPE)?.let { value ->
                type = BytesType.valueOf(value)
            }
            getStringArray(KEY_SOURCE)?.map { NetworkSource.valueOf(it) }?.let { sources ->
                selectedSources.addAll(sources)
            }
            getStringArray(KEY_STATE)?.map { ApplicationState.valueOf(it) }?.let { states ->
                selectedStates.addAll(states)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_lines, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        btn_source_all.setOnClickListener(this)
        btn_source_mobile.setOnClickListener(this)
        btn_source_wifi.setOnClickListener(this)
        btn_state_all.setOnClickListener(this)
        btn_state_foreground.setOnClickListener(this)
        btn_state_background.setOnClickListener(this)
        updateSourceButton(NetworkSource.ALL, btn_source_all_back)
        updateSourceButton(NetworkSource.MOBILE, btn_source_mobile_back)
        updateSourceButton(NetworkSource.WIFI, btn_source_wifi_back)
        updateStateButton(ApplicationState.ALL, btn_state_all_back)
        updateStateButton(ApplicationState.FOREGROUND, btn_state_foreground_back)
        updateStateButton(ApplicationState.BACKGROUND, btn_state_background_back)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_source_all -> updateSource(NetworkSource.ALL, btn_source_all_back)
            R.id.btn_source_mobile -> updateSource(NetworkSource.MOBILE, btn_source_mobile_back)
            R.id.btn_source_wifi -> updateSource(NetworkSource.WIFI, btn_source_wifi_back)
        }
        when (v?.id) {
            R.id.btn_state_all -> updateState(ApplicationState.ALL, btn_state_all_back)
            R.id.btn_state_foreground -> updateState(
                ApplicationState.FOREGROUND,
                btn_state_foreground_back
            )
            R.id.btn_state_background -> updateState(
                ApplicationState.BACKGROUND,
                btn_state_background_back
            )
        }
        updateLines()
    }

    private fun updateSource(source: NetworkSource, buttonBack: FrameLayout) {
        if (selectedSources.contains(source)) {
            if (selectedSources.size == 1) return
            selectedSources.remove(source)
        } else {
            selectedSources.add(source)
        }
        updateSourceButton(source, buttonBack)
    }

    private fun updateState(state: ApplicationState, buttonBack: FrameLayout) {
        if (selectedStates.contains(state)) {
            if (selectedStates.size == 1) return
            selectedStates.remove(state)
        } else {
            selectedStates.add(state)
        }
        updateStateButton(state, buttonBack)
    }

    private fun updateSourceButton(source: NetworkSource, buttonBack: FrameLayout) {
        if (selectedSources.contains(source)) {
            buttonBack.setBackgroundColor(
                requireContext().color(android.R.color.darker_gray)
            )
        } else {
            buttonBack.setBackgroundColor(
                requireContext().color(android.R.color.transparent)
            )
        }
    }

    private fun updateStateButton(state: ApplicationState, buttonBack: FrameLayout) {
        if (selectedStates.contains(state)) {
            buttonBack.setBackgroundColor(
                requireContext().color(android.R.color.darker_gray)
            )
        } else {
            buttonBack.setBackgroundColor(
                requireContext().color(android.R.color.transparent)
            )
        }
    }

    private fun updateLines() {
        if (selectedSources.isNotEmpty()) {
            type?.let {
                listener?.onSourceSelected(it, selectedSources)
            }
        }
        if (selectedStates.isNotEmpty()) {
            type?.let {
                listener?.onStateSelected(it, selectedStates)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listener = null
    }

    interface OnGraphSelected {
        fun onSourceSelected(bytesType: BytesType, sources: ArrayList<NetworkSource>)
        fun onStateSelected(bytesType: BytesType, states: ArrayList<ApplicationState>)
    }

    companion object {
        private val KEY_SOURCE = "source"
        private val KEY_STATE = "state"
        private val KEY_TYPE = "type"
        var listener: OnGraphSelected? = null

        fun getInstance(
            listener: OnGraphSelected,
            bytesType: BytesType,
            sources: ArrayList<NetworkSource>? = null,
            states: ArrayList<ApplicationState>? = null
        ) = LinesBottomSheetDialog().apply {
            this@Companion.listener = listener
            arguments = Bundle().apply {
                sources?.let {
                    putStringArray(KEY_SOURCE, sources.map { it.name }.toTypedArray())
                }
                states?.let {
                    putStringArray(KEY_STATE, states.map { it.name }.toTypedArray())
                }
                putString(KEY_TYPE, bytesType.name)
            }
        }
    }
}