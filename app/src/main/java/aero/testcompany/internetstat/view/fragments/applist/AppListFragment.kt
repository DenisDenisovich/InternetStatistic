package aero.testcompany.internetstat.view.fragments.applist

import aero.testcompany.internetstat.R
import aero.testcompany.internetstat.view.MainActivity
import aero.testcompany.internetstat.util.gone
import aero.testcompany.internetstat.util.hideKeyboard
import aero.testcompany.internetstat.util.showKeyboard
import aero.testcompany.internetstat.util.visible
import aero.testcompany.internetstat.view.BackPressed
import aero.testcompany.internetstat.viewmodel.AppListViewModel
import android.os.Bundle
import android.text.Editable
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.fragment_application_list.*

class AppListFragment: Fragment(), BackPressed, SwipeRefreshLayout.OnRefreshListener {

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
        swiperefresh_list.setOnRefreshListener(this)
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
            et_search.setText("")
            btn_search.gone()
            et_search.visible()
            tv_title.gone()
            et_search.showKeyboard()
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

    override fun onRefresh() {
        swiperefresh_list.isRefreshing = false
        if (et_search.visibility == View.VISIBLE) {
            et_search.gone()
            et_search.hideKeyboard()
            tv_title.visible()
            btn_search.visible()
        }
        (activity as? MainActivity)?.updateApiStatistic()
        progress_app.visible()
        rv_app.gone()
        viewModel.update()
    }
}