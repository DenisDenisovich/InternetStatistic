package aero.testcompany.internetstat.view.fragments.appinfo

import aero.testcompany.internetstat.models.BytesType
import aero.testcompany.internetstat.models.NetworkSource
import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment


class NetworkSourceDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        return builder
            .setTitle("Select source")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage("Для закрытия окна нажмите ОК")
            .setPositiveButton("OK", null)
            .setNegativeButton("Отмена", null)
            .create()
    }

    companion object {
        private val KEY_SOURCE = "source"
        private val KEY_TYPE = "type"
        fun getInstance(source: NetworkSource, bytesType: BytesType) = DialogFragment().apply {
            arguments = Bundle().apply {
                putInt(KEY_SOURCE, source.ordinal)
                putInt(KEY_TYPE, bytesType.ordinal)
            }
        }
    }
}