package com.github.billman64.nycschoolssatscores.Model

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.github.billman64.nycschoolssatscores.R
import kotlinx.android.synthetic.main.school_item.view.*

class SchoolAdapter(private val schoolList:ArrayList<School>): RecyclerView.Adapter<SchoolAdapter.SchoolViewHolder>() {
    val TAG:String = "SAT data demo"

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

        // Dynamix text resizing
        Log.d(TAG, "${currentItem.schoolName.substring(0,11)}... length: ${currentItem.schoolName.length} textSize: ${holder.schoolView.textSize}")

        when(holder.schoolView.text.length){
            in 1..29 -> {
                holder.schoolView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                Log.d(TAG, " --> textSize: 16f")
            }
            in 30..40 -> {
                holder.schoolView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                Log.d(TAG, " --> textSize: 14f")
            }
            in 41..150 -> {
                holder.schoolView. setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                Log.d(TAG, " --> textSize: 12f")
            }
        }

        holder.itemView.animation = AnimationUtils.loadAnimation(holder.itemView.context,R.anim.fade_translate) // minimal animation
        // The less is done here, the better the performance, since onBindViewHolder is called frequently.

        // Make item clickable, calling dialog with SAT scores
        holder.itemView.setOnClickListener( View.OnClickListener {

//            Toast.makeText(, "SAT score data to be implemented",Toast.LENGTH_SHORT).show()
            Log.d(TAG, "item clicked: ${holder.schoolView.text} length: ${holder.schoolView.length()} textSize: ${holder.schoolView.textSize}")
//            val i: Intent(applicationContext, SchoolInfoActivity)

        })
    }

    override fun getItemCount() = schoolList.size   // get # of items in list

}