package com.github.billman64.nycschoolssatscores.View

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.billman64.nycschoolssatscores.Model.School
import com.github.billman64.nycschoolssatscores.Model.SchoolAdapter
import com.github.billman64.nycschoolssatscores.Model.SchoolsAPI
import com.github.billman64.nycschoolssatscores.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    val TAG:String = "SAT data demo"
    private var schoolList = ArrayList<School>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // set up recyclerView, which is used to hold list of tappable school names
        val rv:RecyclerView = findViewById(R.id.recyclerView)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = SchoolAdapter(ArrayList())

        getSchoolData()

        refreshButton.setOnClickListener{
            getSchoolData()
        }



    }




    fun getSchoolData(){

        // Progress bar
        progress_bar.visibility = View.VISIBLE
        progress_bar.isShown

        // Retrofit builder
        val schoolApi =Retrofit.Builder()
            .baseUrl("https://data.cityofnewyork.us/resource/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SchoolsAPI::class.java)
        Log.d(TAG, "Retrofit for schools api created: $schoolApi.toString()")

        // API call via coroutine
        GlobalScope.launch(Dispatchers.IO){
            Log.d(TAG, "coroutine")

            try{
                val responseSchools = schoolApi.getSchools().awaitResponse()    //TODO: ERROR - expected gson.JsonObject
                Log.d(TAG, " response received. code: ${responseSchools.code()} size: ${responseSchools.message()}")

                if(responseSchools.isSuccessful){

                    Log.d(TAG, " response is successful! code+msg: " + responseSchools.code() +" "+ responseSchools.message())
                    Log.d(TAG, " response is successful! body: " + responseSchools.body().toString().substring(0,100))
                    val data = responseSchools.body()!!.getAsJsonArray()
                    Log.d(TAG, " data response successful! Length of data: " + data.toString().substring(0,100))

                    // add each school object to list before updating recyclerView
                    for(i in 0 until data.size()){
                        val s = School(
                            data[i].asJsonObject.get("dbn").toString(),
                            data[i].asJsonObject.get("school_name").toString().replace("Â","") //replace("^\"|\"$","")
                        )

                        // Truncate quote marks at the start and end of school name
                        val temp:String = s.schoolName
//                        Log.d(TAG, " !!! temp first char: ${temp.substring(0,1)}")
                        Log.d(TAG, " !!! temp last char: ${temp.substring(temp.length-1,temp.length)}")
                        if(temp.substring(0,1).equals("\"") and temp.substring(temp.length-1,temp.length).equals("\"")){
                            s.schoolName = temp.substring(1,temp.length-1)
                            if(s.schoolName.substring(1,2) == "Ac") Log.d(TAG, " !!! school name: ${s.schoolName}")
                        }

                        schoolList.add(s)
                    }
                    Log.d(TAG, " school list count: ${schoolList.count()}")

                    withContext(Dispatchers.Main){
                        progress_bar.visibility = View.GONE
                        Toast.makeText(applicationContext,"schools found: ${schoolList.count()}", Toast.LENGTH_SHORT).show()
                        // sort
                        schoolList.sortBy { it.schoolName }
                        recyclerView.adapter = SchoolAdapter(schoolList)
                    }

                } else {    // handling for connected but not successful
                    Log.d(TAG, " Not successful. ErrorBody(): ${responseSchools.errorBody().toString().substring(0,100)}")
                    withContext(Dispatchers.Main){
                        progress_bar.visibility = View.GONE
                        refreshButton.visibility = View.VISIBLE
                    }

                }


            } catch(e:Exception){
                Log.d(TAG, "Net error: ${e.toString().substring(0,70)}")

                // update UI
                withContext(Dispatchers.Main){
                    progress_bar.visibility = View.GONE
                    refreshButton.visibility = View.VISIBLE
                }

            }





        }
    }
}
