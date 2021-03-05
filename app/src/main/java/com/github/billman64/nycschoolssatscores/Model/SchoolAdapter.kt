package com.github.billman64.nycschoolssatscores.Model

import android.app.Dialog
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.provider.Settings.Global.getString
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.github.billman64.nycschoolssatscores.R
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog.*
import kotlinx.android.synthetic.main.school_item.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.security.AccessController.getContext

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

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onBindViewHolder(holder: SchoolViewHolder, position: Int) {
        val currentItem = schoolList[position]

        holder.schoolView.text = currentItem.schoolName

        // Dynamic text resizing
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
            Log.d(TAG, "item clicked: ${holder.schoolView.text} dbn: ${schoolList[position].dbn}  pos: ${holder.adapterPosition} length: ${holder.schoolView.length()} textSize: ${holder.schoolView.textSize}")

            // Setup dialog
            val d = Dialog(holder.schoolView.context)
            d.setContentView(R.layout.dialog)
            d.setTitle(R.string.dialog_title)
            d.school.text = holder.schoolView.text
            d.reading.text = schoolList[position].dbn   //

            // Retrofit builder
            val scoresApi = Retrofit.Builder()
                .baseUrl("https://data.cityofnewyork.us/resource/") //?
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ScoresAPI::class.java)
            Log.d(TAG, "Retrofit for scores api created: ${scoresApi.toString()}")

            // API call via coroutine
            GlobalScope.launch(Dispatchers.IO){

                try {
                    var dbn = schoolList[position].dbn
                    Log.d(TAG, "dbn: $dbn")
                    dbn = dbn.substring(1,dbn.length-1) // remove quote marks

                    val responseScores = scoresApi.getScores(dbn).awaitResponse()
                    Log.d(TAG," response received. code: ${responseScores.code()} message: ${responseScores.message()}")

                    if(responseScores.isSuccessful){

                        val data = responseScores.body()!!.asJsonArray

                        Log.d(TAG, " response stored. Data size: ${data.size()} count: ${data.count()} string: ${data.toString()}")
                        Log.d(TAG, " Data: ${data.toString()}")

                        Log.d(TAG, " isJsonArray: ${data.isJsonArray()}  isJsonObject: ${data.isJsonObject()}")     // it's a jsonArray

                        if(data.size()>0) {
                            val dataObject =
                                data[0]?.asJsonObject   //TODO: fix runtime error - need handling for null JsonArray (ie: Academy for Soft. Eng. (AFSE))
                            Log.d(TAG, "dataObject size: ${dataObject?.size()}")
                            val reading =
                                dataObject?.get("sat_critical_reading_avg_score").toString()
                            Log.d(TAG, "Mean reading score: " + reading)

                            withContext(Dispatchers.Main){

                                // Update dialog's views with score data
                                dataObject?.let{

                                    // Handling for SAT data unavailable for a particular school (denoted with an "s")
                                    //  i.e: Academy for Health Careers
                                    val s = "s"
                                    if(dataObject.get("sat_critical_reading_avg_score").asString == s &&
                                        dataObject.get("sat_math_avg_score").asString == s &&
                                        dataObject.get("sat_writing_avg_score").asString == s &&
                                        dataObject.get("num_of_sat_test_takers").asString == s
                                    ){
                                        Log.d(TAG, "SAT data unavailable. Resulted in 's' data.")
                                        d.noData.visibility = View.VISIBLE
                                        d.reading.text = s
                                        d.math.text = s
                                        d.writing.text = s
                                        d.test_takers.text = s
                                    } else {

                                        // Normal case - display SAT data
                                        d.reading.text =
                                            dataObject.get("sat_critical_reading_avg_score").asString   //TODO: refactor to reduce code here
                                        d.math.text = dataObject.get("sat_math_avg_score").asString
                                        d.writing.text =
                                            dataObject.get("sat_writing_avg_score").asString
                                        d.test_takers.text =
                                            dataObject.get("num_of_sat_test_takers").asString
                                    }
                                    d.show()
                                }

                            }

                        } else{
                            withContext(Dispatchers.Main) {
                                Log.d(TAG, "Data response is empty for this school")
                                d.noData.visibility = View.VISIBLE
                                val na = holder.schoolView.context.getString(R.string.notAvailable)
                                d.reading.text = na
                                d.math.text = na
                                d.writing.text = na
                                d.test_takers.text = na
                                d.show()
                            }
                        }
                    }

                } catch(e:Exception){
                    Log.d(TAG, " network error: " + e.toString())
                }
            }
                Log.d(TAG, "coroutine")
        })
    }
    override fun getItemCount() = schoolList.size   // get # of items in list
}