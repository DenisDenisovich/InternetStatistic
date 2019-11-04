package aero.testcompany.internetstat.view.fragments.applist

import aero.testcompany.internetstat.view.MainActivity
import aero.testcompany.internetstat.R
import aero.testcompany.internetstat.util.gone
import aero.testcompany.internetstat.util.visible
import aero.testcompany.internetstat.viewmodel.AppListViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_application_list.*


class AppListFragment: Fragment() {

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
            appAdapter.items = ArrayList(it)
            progress_app.gone()
            rv_app.visible()
        })
        rv_app.adapter = appAdapter.apply {
            onItemClicked = {
                (activity as MainActivity).startAppInfo(it)
            }
        }
        viewModel.update()
    }
}