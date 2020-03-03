package com.cocoslide

import android.Manifest
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import org.jsoup.Jsoup
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AppWidgetConfigureActivity : Activity() {


    lateinit var appWidgetManager: AppWidgetManager
    lateinit var appWidgetId: ComponentName

    var date = ""

    lateinit var mLocation: Location
    var address = ""

    lateinit var locationManager:LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.simple_widget);
        setResult(RESULT_CANCELED);

//
//        appWidgetId = intent?.extras?.getInt(
//            AppWidgetManager.EXTRA_APPWIDGET_ID,
//            AppWidgetManager.INVALID_APPWIDGET_ID
//        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        getGPS()
        getData()

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
        address = "${mLocation.latitude}, ${mLocation.longitude}"
//        try {
//            address = geocoder.getFromLocation(
//                mLocation?.latitude?: (-1).toDouble(),
//                mLocation?.longitude?: (-1).toDouble(),
//                1
//            )[0].getAddressLine(0)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
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
