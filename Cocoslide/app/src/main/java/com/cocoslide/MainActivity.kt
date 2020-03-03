package com.cocoslide

import android.appwidget.AppWidgetManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.jsoup.Jsoup

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
    fun getData(){
        var url = "http://ncov.mohw.go.kr/bdBoardList_Real.do?brdId=1&brdGubun=11&ncvContSeq=&contSeq=&board_id=&gubun="
        var queue = Volley.newRequestQueue(this)

        var postRequest = object: StringRequest(
            Method.GET, url,
            object: com.android.volley.Response.Listener<String>{
                override fun onResponse(response: String?) {
                    var data = parseHtml(response!!)
                    var manager = AppWidgetManager.getInstance(this@MainActivity)

                }
            },
            object : com.android.volley.Response.ErrorListener{
                override fun onErrorResponse(error: VolleyError?) {
//                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    Log.v("Error", error.toString())
                }
            }
        ){}
        queue.add(postRequest)
    }
    fun parseHtml(response: String):ArrayList<String>{
        var data = ArrayList<String>()
        var doc = Jsoup.parse(response)
        var current = doc.getElementsByClass("data_table tbl_scrl_mini2 mgt16")[0]
        var tr = current.getElementsByTag("tr")
        for(i in tr){
            var td = i.getElementsByTag("td")[0]
            var count = td.text()
            data.add(count)
            Log.d("Current", count)
        }
        return data
    }


}
