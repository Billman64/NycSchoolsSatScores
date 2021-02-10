package com.github.billman64.nycschoolssatscores.Model

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog.*
import kotlinx.android.synthetic.main.school_item.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception

class SchoolAdapter(private val schoolList:ArrayList<School>): RecyclerView.Adapter<SchoolAdapter.SchoolViewHolder>() {
    val TAG:String = "SAT data demo" + this.javaClass.simpleName

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
//        Log.d(TAG, "${currentItem.schoolName.substring(0,11)}... length: ${currentItem.schoolName.length} textSize: ${holder.schoolView.textSize}")

        when(holder.schoolView.text.length){
            in 1..29 -> {
                holder.schoolView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            }
            in 30..40 -> {
                holder.schoolView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            }
            in 41..150 -> {
                holder.schoolView. setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            }
        }

        holder.itemView.animation = AnimationUtils.loadAnimation(holder.itemView.context,R.anim.fade_translate) // minimal animation
        // The less is done here, the better the performance, since onBindViewHolder is called frequently.

        // Make item clickable, calling dialog with SAT scores
        holder.itemView.setOnClickListener( View.OnClickListener {
//            it.setBackgroundResource(R.drawable.rounded_corners_highlighted) // hightlighting
//            val readingScore: TextView = findViewById(R.id.reading_score)
//            readingScore.text = "123"
//            val reading_score.text = "123"

//            Toast.makeText(, "SAT score data to be implemented",Toast.LENGTH_SHORT).show()
            Log.d(TAG, "item clicked: ${holder.schoolView.text} dbn: ${schoolList[position].dbn}  pos: ${holder.adapterPosition} length: ${holder.schoolView.length()} textSize: ${holder.schoolView.textSize}")
//            val i: Intent(applicationContext, SchoolInfoActivity)

            var d = Dialog(holder.schoolView.context)
            d.setContentView(R.layout.dialog)
            d.setTitle(R.string.dialog_title)
            d.school.text = holder.schoolView.text
            d.reading.text = schoolList[position].dbn

            //TODO: call scoresAPI, use school id # (dbn)

            // Retrofit builder
            val scoresApi = Retrofit.Builder()
                .baseUrl("https://data.cityofnewyork.us/resource/") //?
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ScoresAPI::class.java)
            Log.d(TAG, "Retrofit for scores api created: $scoresApi.toString()")


            // API call via coroutine
            GlobalScope.launch(Dispatchers.IO){

                try {
                    val responseScores = scoresApi.getScores(schoolList[position].dbn).awaitResponse()
                    Log.d(TAG," response received. code: ${responseScores.code()} size: ${responseScores.message()}")

                    if(responseScores.isSuccessful){
                        Log.d(TAG, " response is successful")

                        val data = responseScores.body()!!.asJsonArray
                        Log.d(TAG, " response stored")

                        Log.d(TAG, " isJsonArray: ${data.isJsonArray()}  isJsonObject: ${data.isJsonObject()}")     // it's a jsonArray

                        var reading = data.asJsonArray.get(0).asJsonObject.get("sat_critical_reading_avg_score").toString()        //TODO: fix error here


                        Log.d(TAG, "Mean reading score: " + reading)

                    }


                } catch(e:Exception){
                    Log.d(TAG, " network error: " + e.toString())

                }



            }
                Log.d(TAG, "coroutine")




            d.show()
        })
    }

    override fun getItemCount() = schoolList.size   // get # of items in list

}