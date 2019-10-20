package aero.testcompany.internetstat.applist

import aero.testcompany.internetstat.R
import aero.testcompany.internetstat.models.MyPackageInfo
import aero.testcompany.internetstat.models.NetworkInfo
import aero.testcompany.internetstat.util.toMb
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_package_info.view.*

class ApplicationListAdapter: RecyclerView.Adapter<ApplicationListAdapter.ApplicationViewHolder>() {

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
        ): RecyclerView.ViewHolder(view) {

        @SuppressLint("SetTextI18n")
        fun bind(networkInfo: MyPackageInfo) {
            icon.setImageDrawable(itemView.context.packageManager.getApplicationIcon(networkInfo.packageName))
            label.text = networkInfo.name
            packageName.text = networkInfo.packageName
        }
    }
}