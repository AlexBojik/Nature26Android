package ru.bizit.nature26.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_feature.view.*
import kotlinx.android.synthetic.main.item_layer.view.*
import kotlinx.android.synthetic.main.item_layer.view.feature_color
import kotlinx.android.synthetic.main.item_layer.view.feature_name
import ru.bizit.nature26.*
import java.util.*

class FeatureListAdapter(private val featureList: MutableList<Feature>, private val appData: AppData): RecyclerView.Adapter<FeatureListAdapter.MyViewHolder>() {
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    private lateinit var parent: ViewGroup

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        this.parent = parent
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feature, parent, false) as View
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val current = featureList[position]
        holder.itemView.feature_name.text = current.name
        holder.itemView.description.loadData(current.description, "", "")

        val id = this.parent.resources?.getIdentifier(current.symbol, "mipmap", parent.context.packageName)
        if (id != null) {
            holder.itemView.feature_image.setImageResource(id)
        } else {
            holder.itemView.feature_image.visibility = View.INVISIBLE
        }

        if (!current.color.isNullOrEmpty()) {
            holder.itemView.feature_color.setColorFilter(current.color!!.toColorInt())
        }
    }

    override fun getItemCount() = featureList.size
}

class ExpandableListAdapter(private val layerList: MutableList<Layer>): BaseExpandableListAdapter() {
    override fun getChild(groupPosition: Int, childPosition: Int): Layer? {
        return layerList[groupPosition].layers?.get(childPosition)
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val itemView = LayoutInflater.from(parent?.context).inflate(R.layout.item_layer, parent, false)

        val item = layerList[groupPosition].layers?.get(childPosition)


        if (item != null) {
            itemView.check.isChecked = item.visible
            itemView.check.setOnCheckedChangeListener { _, isChecked ->
                item.visible = isChecked
            }

            itemView.feature_name.text = item.name
            if (!item.color.isNullOrEmpty()) {
                val color = item.color?.toUpperCase(Locale.ROOT)?.toColorInt()
                color?.let { itemView.feature_color.setColorFilter(it) }
            }
        }
        val id = parent?.resources?.getIdentifier(item?.symbol, "mipmap", parent.context.packageName)
        if (id != null) {
            itemView.iconImage.setImageResource(id)
        } else {
            itemView.iconImage.visibility = View.INVISIBLE
        }


        return itemView
    }


    override fun getChildrenCount(groupPosition: Int): Int {
        return layerList[groupPosition].layers?.count()!!
    }

    override fun getGroup(groupPosition: Int): Any {
        return layerList[groupPosition]
    }

    override fun getGroupCount(): Int {
        return layerList.count()
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val itemView = LayoutInflater.from(parent?.context).inflate(R.layout.item_layer, parent, false)
        val item = layerList[groupPosition]

        itemView.check.isChecked = item.visible
        itemView.check.setOnCheckedChangeListener { _, isChecked ->
            item.visible = isChecked
            item.layers?.forEach { child -> child.visible = isChecked }
            notifyDataSetChanged()
        }

        itemView.feature_name.text = item.name
        itemView.feature_color.visibility = View.INVISIBLE
        itemView.feature_color.layoutParams.width = 0
        val id = parent?.resources?.getIdentifier(item.icon, "drawable", parent.context.packageName)
        if (id != null) {
            itemView.iconImage.setImageResource(id)
        }
        return itemView
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}