package aero.testcompany.internetstat.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class MyPackageInfo(val name: String, val packageName: String, val version: String): Parcelable