package aero.testcompany.internetstat.viewmodel

import aero.testcompany.internetstat.domain.packageinfo.GetPackagesUseCase
import aero.testcompany.internetstat.models.MyPackageInfo
import android.content.pm.PackageManager
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppListViewModel : ViewModel() {
    var packageManager: PackageManager? = null
        set(value) {
            field = value
            value?.let {
                getPackagesUseCase =
                    GetPackagesUseCase(value)
            }
        }
    val installedPackages: MutableLiveData<List<MyPackageInfo>> by lazy {
        MutableLiveData<List<MyPackageInfo>>()
    }
    private lateinit var getPackagesUseCase: GetPackagesUseCase

    fun update() {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                installedPackages.postValue(getPackagesUseCase.getPackages())
            }
        }
    }
}