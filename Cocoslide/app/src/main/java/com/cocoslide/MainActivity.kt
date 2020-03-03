package com.cocoslide

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import org.jsoup.Jsoup

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        permissionCheck()
        getGPS()
    }

    fun permissionCheck(){

        var permissionListener = object: PermissionListener{
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
    }

    lateinit var mLocation:Location
    var address = ""

    fun getGPS(){

        var locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

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
        var geocoder = Geocoder(this)
        try {
            address = geocoder.getFromLocation(
//                33.269000, 126.612434,
                mLocation.latitude,
                mLocation.longitude,
                1
            )[0].getAddressLine(0)
            Log.v("Location", "${mLocation.latitude}, ${mLocation.longitude}")
            Log.v("Location", address)

        } catch (e: Exception) {
            e.printStackTrace()
        }
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
