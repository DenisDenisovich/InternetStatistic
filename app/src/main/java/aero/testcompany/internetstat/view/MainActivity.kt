package aero.testcompany.internetstat.view

import aero.testcompany.internetstat.R
import aero.testcompany.internetstat.models.MyPackageInfo
import aero.testcompany.internetstat.view.fragments.appinfo.AppInfoFragment
import aero.testcompany.internetstat.view.fragments.applist.AppListFragment
import android.Manifest
import android.annotation.TargetApi
import android.app.AppOpsManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val intent = Intent(this, StatisticService::class.java)
        intent.putExtra("timeStamp", System.currentTimeMillis())
        startService(intent)
    }

    override fun onResume() {
        super.onResume()
        if (hasPermissionToReadNetworkHistory()) {
            if (!hasPermissionToReadPhoneStats()) {
                requestPhoneStateStats()
            } else if (supportFragmentManager.backStackEntryCount == 0) {
                startAppList()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            READ_PHONE_STATE_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    supportFragmentManager.popBackStack(
                        null,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    startAppList()
                }
                return
            }
        }
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.fragments.lastOrNull() as? BackPressed
        if (currentFragment is BackPressed) {
            if (!currentFragment.onBackPressed()) {
                goBack()
            }
        } else {
            goBack()
        }
    }

    private fun goBack() {
        if (supportFragmentManager.backStackEntryCount == 1) {
            finish()
        } else {
            super.onBackPressed()
        }
    }

    private fun startAppList() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.container, AppListFragment())
            addToBackStack(null)
            commit()
        }
    }


    fun startAppInfo(info: MyPackageInfo) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.container, AppInfoFragment.getInstance(info))
            addToBackStack(null)
            commit()
        }
    }

    @Suppress("DEPRECATED_IDENTITY_EQUALS")
    private fun hasPermissionToReadPhoneStats(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_PHONE_STATE
        ) !== PackageManager.PERMISSION_DENIED
    }

    private fun requestPhoneStateStats() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_PHONE_STATE), READ_PHONE_STATE_REQUEST
        )
    }

    private fun hasPermissionToReadNetworkHistory(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(), packageName
        )
        if (mode == AppOpsManager.MODE_ALLOWED) {
            return true
        }
        appOps.startWatchingMode(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            applicationContext.packageName,
            object : AppOpsManager.OnOpChangedListener {
                @TargetApi(Build.VERSION_CODES.M)
                override fun onOpChanged(op: String, packageName: String) {
                    val mode = appOps.checkOpNoThrow(
                        AppOpsManager.OPSTR_GET_USAGE_STATS,
                        android.os.Process.myUid(), getPackageName()
                    )
                    if (mode != AppOpsManager.MODE_ALLOWED) {
                        return
                    }
                    appOps.stopWatchingMode(this)
                }
            })
        requestReadNetworkHistoryAccess()
        return false
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun requestReadNetworkHistoryAccess() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivity(intent)
    }

    companion object {
        const val READ_PHONE_STATE_REQUEST = 37
    }
}
