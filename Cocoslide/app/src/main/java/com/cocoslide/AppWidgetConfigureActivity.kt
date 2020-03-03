package com.cocoslide

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.jsoup.Jsoup
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AppWidgetConfigureActivity : Activity() {


    lateinit var appWidgetManager: AppWidgetManager
    lateinit var appWidgetId: ComponentName

    var date = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.simple_widget);
        setResult(RESULT_CANCELED);

//
//        appWidgetId = intent?.extras?.getInt(
//            AppWidgetManager.EXTRA_APPWIDGET_ID,
//            AppWidgetManager.INVALID_APPWIDGET_ID
//        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        getData()

    }


    fun getData(){
        var url = "http://ncov.mohw.go.kr/bdBoardList_Real.do?brdId=1&brdGubun=11&ncvContSeq=&contSeq=&board_id=&gubun="
        var queue = Volley.newRequestQueue(this)

        var postRequest = object: StringRequest(
            Method.GET, url,
            object: com.android.volley.Response.Listener<String>{
                override fun onResponse(response: String?) {

                    appWidgetId = ComponentName(baseContext, SimpleWidget::class.java)
                    appWidgetManager = AppWidgetManager.getInstance(baseContext)


                    var now = LocalDateTime.now()
                    var formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm")
                    date = now.format(formatter)
                    Log.d("Date", date)

                    var data = parseHtml(response!!)

                    RemoteViews(this@AppWidgetConfigureActivity.packageName, R.layout.simple_widget).also { views->
                        views.setTextViewText(R.id.total, data[0])
                        views.setTextViewText(R.id.dead, data[2])
                        views.setTextViewText(R.id.clear, data[1])
                        views.setTextViewText(R.id.date, date)
//                        views.setOnClickResponse(R.id.refreshBtn, )
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                    val resultValue = Intent().apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    }
                    setResult(Activity.RESULT_OK, resultValue)
                    finish()

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
