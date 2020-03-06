package com.cocoslide

import android.Manifest
import android.app.*
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
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


    lateinit var mLocation: Location
    var address = ""
    var localCount = ""
    lateinit var locationManager:LocationManager

    override fun onCreate() {
        super.onCreate()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            Log.v("SERVICE ", "Fore:" + Build.VERSION.SDK_INT.toString())

            val channer = NotificationChannel(
                channelId, channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                setShowBadge(false)
            }
            var manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channer)
            var notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_SYSTEM)

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
        getGPS()
    }

    fun getGPS(){

        var permissionListener = object: PermissionListener {
            override fun onPermissionGranted() {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
        TedPermission.with(this)
            .setPermissionListener(permissionListener)
            .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
            .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION , Manifest.permission.ACCESS_FINE_LOCATION)
            .check();

        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val locationListener = object: LocationListener {
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String?) {}
            override fun onProviderDisabled(provider: String?) {}
            override fun onLocationChanged(location: Location?) {
                mLocation = location!!
                makeAddress()
                getLocalData()
                locationManager.removeUpdates(this)
            }
        }

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            if(networkLocation!=null) {
                mLocation = networkLocation
                makeAddress()
            }

            val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if(gpsLocation!=null) {
                mLocation = gpsLocation
                makeAddress()
            }

        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1f, locationListener)
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1f, locationListener)

    }
    private fun makeAddress() {
        Log.v("Location", "${mLocation.latitude}, ${mLocation.longitude}")
//        address = "${mLocation.latitude}, ${mLocation.longitude}"
        var geocoder = Geocoder(this)
        try {
            address = geocoder.getFromLocation(
                mLocation.latitude,
                mLocation.longitude,
                1
            )[0].getAddressLine(0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun getLocalData(){
        var url = "http://ncov.mohw.go.kr/bdBoardList_Real.do?brdId=1&brdGubun=13&ncvContSeq=&contSeq=&board_id=&gubun="
        var queue = Volley.newRequestQueue(this)

        var postRequest = object: StringRequest(
            Method.GET, url,
            object: com.android.volley.Response.Listener<String>{
                override fun onResponse(response: String?) {
                    var doc = Jsoup.parse(response)

                    if(doc.getElementsByClass("data_table mgt24") != null){
                        var local = doc.getElementsByTag("tbody")[0]
                        var tr = local.getElementsByTag("tr")

                        var mLocal = address.split(" ")[1]
                        when(mLocal){
                            "서울특별시"->{
                                localCount = tr[1].getElementsByTag("td")[1].text()
                            }
                            "부산광역시"->{
                                localCount = tr[2].getElementsByTag("td")[1].text()
                            }
                            "대구광역시"->{
                                localCount = tr[3].getElementsByTag("td")[1].text()
                            }
                            "인천광역시"->{
                                localCount = tr[4].getElementsByTag("td")[1].text()
                            }
                            "광주광역시"->{
                                localCount = tr[5].getElementsByTag("td")[1].text()
                            }
                            "대전광역시"->{
                                localCount = tr[6].getElementsByTag("td")[1].text()
                            }
                            "울산광역시"->{
                                localCount = tr[7].getElementsByTag("td")[1].text()
                            }
//                            "세종"->{
//                                localCount = tr[8].getElementsByTag("td")[1].text()
//                            }
                            "경기도"->{
                                localCount = tr[9].getElementsByTag("td")[1].text()
                            }
                            "강원도"->{
                                localCount = tr[10].getElementsByTag("td")[1].text()
                            }
                            "충청북도"->{
                                localCount = tr[11].getElementsByTag("td")[1].text()
                            }
                            "충청남도"->{
                                localCount = tr[12].getElementsByTag("td")[1].text()
                            }
                            "전라북도"->{
                                localCount = tr[13].getElementsByTag("td")[1].text()
                            }
                            "전라남도"->{
                                localCount = tr[14].getElementsByTag("td")[1].text()
                            }
                            "경상북도"->{
                                localCount = tr[15].getElementsByTag("td")[1].text()
                            }
                            "경상남도"->{
                                localCount = tr[16].getElementsByTag("td")[1].text()
                            }
                            "제주특별자치도"->{
                                localCount = tr[17].getElementsByTag("td")[1].text()
                            }

                        }
                        for(i in tr){
                            var td = i.getElementsByTag("td")[0]
                            var count = td.text()
                        }
                    }

                    getData()

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

    fun getData(){
//        var url = "http://ncov.mohw.go.kr/bdBoardList_Real.do?brdId=1&brdGubun=11&ncvContSeq=&contSeq=&board_id=&gubun="
        var url = "https://wuhanvirus.kr/"

        var queue = Volley.newRequestQueue(this)

        var postRequest = object: StringRequest(
            Method.GET, url,
            object: com.android.volley.Response.Listener<String>{
                override fun onResponse(response: String?) {
                    var now = LocalDateTime.now()
                    var formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm")
                    var date = now.format(formatter)

                    Log.d("Service Date", date)

                    var data = parseHtml(response!!)


                    var notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle(address.split(" ")[1] + ": ${localCount} 명")
                        .setContentText("전국 : ${data[0]} 완치: ${data[1]} 사망 : ${data[2]}")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_SYSTEM)
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
                        views.setTextViewText(R.id.local, address.split(" ")[1])
                        views.setTextViewText(R.id.l_count, ": ${localCount} 명")

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
//        Log.v("HTML", doc.toString())

        if(doc.getElementsByClass("data_table mgt16") != null){
            var current = doc.getElementsByTag("tbody")[0]
            var tr = current.getElementsByTag("tr")
            for(i in tr){
                var td = i.getElementsByTag("td")[0]
                var count = td.text()
                data.add(count)
                Log.d("Current", count)
            }
        }
        
        return data
    }

}