package aero.testcompany.internetstat.view.fragments.applist

import aero.testcompany.internetstat.R
import aero.testcompany.internetstat.models.MyPackageInfo
import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_package_info.view.*
import kotlinx.coroutines.*

class AppListAdapter : RecyclerView.Adapter<AppListAdapter.ApplicationViewHolder>() {

    var items: ArrayList<MyPackageInfo> = arrayListOf()
    var onItemClicked: ((packageInfo: MyPackageInfo) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationViewHolder {
        return ApplicationViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_package_info, parent, false)
        ).apply {
            itemView.setOnClickListener {
                onItemClicked?.invoke(items[adapterPosition])
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class ApplicationViewHolder(
        view: View,
        var label: TextView = view.tv_name,
        var packageName: TextView = view.tv_package,
        var icon: ImageView = view.iv_icon
    ) : RecyclerView.ViewHolder(view) {

        private var imageJob = Job()
        private var uiScope = CoroutineScope(Dispatchers.Main + imageJob)
        private var imageOperation: Deferred<Drawable>? = null

        @SuppressLint("SetTextI18n")
        fun bind(packageInfo: MyPackageInfo) {
            uiScope.launch {
                if (imageOperation?.isActive == true) {
                    imageOperation?.cancelAndJoin()
                }
                icon.setImageResource(android.R.color.transparent)
                imageOperation = async(Dispatchers.Default) {
                    itemView.context.packageManager.getApplicationIcon(packageInfo.packageName)
                }
                val drawable = imageOperation?.await()
                icon.setImageDrawable(drawable)
            }
            label.text = packageInfo.name
            packageName.text = packageInfo.packageName
        }
    }
}