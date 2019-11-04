package aero.testcompany.internetstat.domain.packageinfo

import android.content.Context
import android.content.pm.PackageManager

class GetPackageUidUseCase(private val context: Context) {

    fun getUid(packageName: String): Int {
        return try {
            context.packageManager
                .getPackageInfo(packageName, PackageManager.GET_META_DATA)
                .applicationInfo
                .uid
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            -1
        }
    }
}