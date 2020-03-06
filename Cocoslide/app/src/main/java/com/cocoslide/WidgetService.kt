package com.cocoslide

import android.app.*
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.jsoup.Jsoup
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WidgetService: Service() {
    override fun onBind(p0: Intent?): IBinder? {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return null
    }
    val CLICK = "REFRESH_CLICK"

    lateinit var appWidgetManager: AppWidgetManager
    lateinit var componentName: ComponentName


    val channelId = "com.cocoslide"
    val channelName = "Coco"

    override fun onCreate() {
        super.onCreate()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            Log.v("SERVICE ", "Fore:" + Build.VERSION.SDK_INT.toString())

            val channer = NotificationChannel(
                channelId, channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            var manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channer)
            var notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("코코")
                .setContentText("Today")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
            var notification  = notificationBuilder.build()
            val NOTIFICATION_ID = 11111
            startForeground(NOTIFICATION_ID, notification)
        } else {
            Log.v("SERVICE", Build.VERSION.SDK_INT.toString())

        }
    }

    override fun onStart(intent: Intent?, startId: Int) {
//        super.onStart(intent, startId)
        componentName = ComponentName(this, SimpleWidget::class.java)
        appWidgetManager = AppWidgetManager.getInstance(this)
        getData()
    }

    fun getData(){
//        var url = "http://ncov.mohw.go.kr/bdBoardList_Real.do?brdId=1&brdGubun=11&ncvContSeq=&contSeq=&board_id=&gubun="
        var url = "https://wuhanvirus.kr/"

        var queue = Volley.newRequestQueue(this)

        var postRequest = object: StringRequest(
            Method.GET, url,
            object: com.android.volley.Response.Listener<String>{
                override fun onResponse(response: String?) {
                    var now = LocalDateTime.now()
                    var formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm:ss")
                    var date = now.format(formatter)

                    Log.d("Service Date", date)

                    var data = parseHtml(response!!)
                    var notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle("코코")
                        .setContentText("확진자: ${data[0]} 사망자: ${data[2]} 격리해제: ${data[1]}")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_CALL)
                    var notification  = notificationBuilder.build()
                    val NOTIFICATION_ID = 11111
                    startForeground(NOTIFICATION_ID, notification)

                    var intent = Intent(baseContext, SimpleWidget::class.java)
                    intent.setAction("com.cocoslide.REFRESH_CLICK")
                    var pendingSync = PendingIntent.getBroadcast(applicationContext, 0, intent,0)

//                    var appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
//                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
//                    baseContext.sendBroadcast(intent)

                    RemoteViews(packageName, R.layout.simple_widget).also { views->
                        views.setTextViewText(R.id.total, data[0])
                        views.setTextViewText(R.id.dead, data[2])
                        views.setTextViewText(R.id.clear, data[1])
                        views.setTextViewText(R.id.date, date)
                        views.setOnClickPendingIntent(R.id.refreshBtn, pendingSync)
                        appWidgetManager.updateAppWidget(componentName, views)
                    }
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
//        var current = doc.getElementsByClass("data_table mgt16")[0]
//        var tr = current.getElementsByTag("tr")
//        for(i in tr){
//            var td = i.getElementsByTag("td")[0]
//            var count = td.text()
//            data.add(count)
//            Log.d("Current", count)
//        }
        var infected_num = doc.getElementsByClass("infected number")[1].text()
        var death_num = doc.getElementsByClass("death red number")[1].text()
        var released_num = doc.getElementsByClass("released number")[1].text()
        data.add(infected_num)
        data.add(released_num)
        data.add(death_num)

        return data
    }
}