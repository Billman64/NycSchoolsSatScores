package com.github.billman64.nycschoolssatscores.Model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.billman64.nycschoolssatscores.R
import kotlinx.android.synthetic.main.school_item.view.*

class SchoolAdapter(private val schoolList:ArrayList<School>): RecyclerView.Adapter<SchoolAdapter.SchoolViewHolder>() {

    class SchoolViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        // View references of an individual item
        val schoolView:TextView = itemView.school
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SchoolViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.school_item, parent,false)
        return SchoolViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SchoolViewHolder, position: Int) {
        val currentItem = schoolList[position]

        holder.schoolView.text = currentItem.schoolName
        holder.itemView.animation = AnimationUtils.loadAnimation(holder.itemView.context,R.anim.fade_translate) // minimal animation
        // The less is done here, the better the performance, since onBindViewHolder is called frequently.
    }

    override fun getItemCount() = schoolList.size   // get # of items in list

}