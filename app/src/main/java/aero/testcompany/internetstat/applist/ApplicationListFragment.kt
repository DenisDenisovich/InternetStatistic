package aero.testcompany.internetstat.applist

import aero.testcompany.internetstat.MainActivity
import aero.testcompany.internetstat.R
import aero.testcompany.internetstat.models.MyPackageInfo
import aero.testcompany.internetstat.models.NetworkInfo
import aero.testcompany.internetstat.util.PackageManagerHelper
import aero.testcompany.internetstat.util.PackageNetworkInfo
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_application_list.*

class ApplicationListFragment: Fragment() {

    lateinit var networkStatsManager: NetworkStatsManager
    lateinit var adapter: ApplicationListAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        networkStatsManager = activity!!.applicationContext.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        adapter = ApplicationListAdapter()
        return inflater.inflate(R.layout.fragment_application_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rv_app.adapter = adapter
        adapter.items = ArrayList(getData())
        adapter.onItemClicked = {
            (activity as MainActivity).startAppInfo(it)
        }
    }

    private fun getData(): List<MyPackageInfo> {
        val packagesHelper = PackageManagerHelper()
        return packagesHelper.getPackagesData(activity!!.packageManager)
    }
}