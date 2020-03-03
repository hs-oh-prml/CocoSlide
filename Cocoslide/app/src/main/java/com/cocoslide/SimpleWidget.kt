package com.cocoslide

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import java.time.LocalDateTime

/**
 * Implementation of App Widget functionality.
 */
class SimpleWidget : AppWidgetProvider() {


    val CLICK = "com.cocoslide.REFRESH_CLICK"
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            var intent =Intent(context, WidgetService::class.java)
//            Log.v("UPDATE", appWidgetId.toString())
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            Log.v("UPDATE", "Foreground")
                context.startForegroundService(intent)
            } else {
                Log.v("UPDATE", "Background")
                context.startService(intent)
            }
//            context.startService(intent)
//            context.startForegroundService(intent)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.v("Recieve", intent?.action)
        if(intent?.action == "com.cocoslide.REFRESH_CLICK"){
            var intent =Intent(context, WidgetService::class.java)
//            context?.startService(intent)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                Log.v("UPDATE", "Foreground")
                context?.startForegroundService(intent)
            } else {
                Log.v("UPDATE", "Background")
                context?.startService(intent)
            }
//            context?.startForegroundService(intent)

            return
        } else {
            super.onReceive(context, intent)
        }

    }
}