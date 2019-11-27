package aero.testcompany.internetstat.view.fragments.applist

import aero.testcompany.internetstat.R
import aero.testcompany.internetstat.view.MainActivity
import aero.testcompany.internetstat.util.gone
import aero.testcompany.internetstat.util.visible
import aero.testcompany.internetstat.view.BackPressed
import aero.testcompany.internetstat.viewmodel.AppListViewModel
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_application_list.*

class AppListFragment: Fragment(), BackPressed {

    private lateinit var appAdapter: AppListAdapter
    private lateinit var viewModel: AppListViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        appAdapter = AppListAdapter()
        viewModel = ViewModelProviders.of(this)[AppListViewModel::class.java]
        viewModel.packageManager = requireContext().packageManager
        return inflater.inflate(R.layout.fragment_application_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.installedPackages.observe(this, Observer {
            appAdapter.setItems(ArrayList(it))
            progress_app.gone()
            rv_app.visible()
        })
        rv_app.adapter = appAdapter.apply {
            onItemClicked = {
                (activity as MainActivity).startAppInfo(it)
            }
        }
        viewModel.update()
        btn_search.setOnClickListener {
            btn_search.gone()
            et_search.visible()
            tv_title.gone()
            openKeyboard()
            TransitionManager.beginDelayedTransition(toolbar)
        }
        et_search.addTextChangedListener { text: Editable? ->
            appAdapter.setFilter(text.toString())
        }
    }

    override fun onBackPressed(): Boolean =
        if (et_search.visibility == View.VISIBLE) {
            et_search.gone()
            tv_title.visible()
            btn_search.visible()
            appAdapter.setFilter("")
            TransitionManager.beginDelayedTransition(toolbar)
            true
        } else {
            false
        }

    fun openKeyboard() {
        et_search.requestFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(et_search, InputMethodManager.SHOW_IMPLICIT)
    }
}