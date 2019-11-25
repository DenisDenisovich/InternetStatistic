package aero.testcompany.internetstat.view.fragments.appinfo

import aero.testcompany.internetstat.R
import aero.testcompany.internetstat.models.ApplicationState
import aero.testcompany.internetstat.models.BytesType
import aero.testcompany.internetstat.models.NetworkSource
import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment


class GraphLineDialog : DialogFragment() {

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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext()).run {
            val dialogType = if (selectedSources.isNotEmpty()) "source" else "state"
            setTitle("Select $type $dialogType")
            setIcon(android.R.drawable.ic_dialog_alert)
            setMultiChoiceItems(
                R.array.sources,
                booleanArrayOf(true, false, false)
            ) { _, which, isChecked ->
                if (selectedSources.isNotEmpty()) {
                    selectedSources.remove(NetworkSource.values()[which])
                    if (isChecked) {
                        selectedSources.add(NetworkSource.values()[which])
                    }
                } else {
                    selectedStates.remove(ApplicationState.values()[which])
                    if (isChecked) {
                        selectedStates.add(ApplicationState.values()[which])
                    }
                }
            }
            setPositiveButton("OK", null)
            setNegativeButton("Cancel", null)
            create()
        }

    override fun onDestroy() {
        super.onDestroy()
        if (selectedSources.isNotEmpty()) {
            type?.let {
                listener?.onSourceSelected(it, selectedSources)
            }
        } else {
            type?.let {
                listener?.onStateSelected(it, selectedStates)
            }
        }
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
        ) = DialogFragment().apply {
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