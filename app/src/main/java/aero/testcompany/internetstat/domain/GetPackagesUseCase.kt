package aero.testcompany.internetstat.domain

import aero.testcompany.internetstat.models.MyPackageInfo
import android.Manifest
import android.content.pm.PackageManager

class GetPackagesUseCase(private val packageManager: PackageManager) {

    fun getPackages(): List<MyPackageInfo> {
        val installedPackages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            .apply {
                sortByDescending { it.lastUpdateTime }
            }
        val myPackagesList = ArrayList<MyPackageInfo>(installedPackages.size)
        for (installedPackage in installedPackages) {
            if (internetAllowed(installedPackage.packageName)) {
                myPackagesList.add(
                    MyPackageInfo(
                        getPackageName(installedPackage.packageName),
                        installedPackage.packageName,
                        installedPackage.versionName
                    )
                )
            }
        }
        return myPackagesList
    }

    private fun getPackageName(packageName: String): String =
        try {
            val ai = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            packageManager.getApplicationLabel(ai).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            packageName
        }

    private fun internetAllowed(packageName: String) =
        packageManager.checkPermission(
            Manifest.permission.INTERNET,
            packageName
        ) != PackageManager.PERMISSION_DENIED
}