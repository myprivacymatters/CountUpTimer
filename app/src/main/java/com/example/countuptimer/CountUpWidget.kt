package com.example.countuptimer

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.widget.RemoteViews
import java.time.LocalDate

class CountUpWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val prefs = context.getSharedPreferences("CountUpPrefs", Context.MODE_PRIVATE)
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, prefs)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, prefs: SharedPreferences) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            val startDateEpoch = prefs.getLong("startDate", LocalDate.now().toEpochDay())
            val labelText = prefs.getString("label", "My Streak") ?: ""
            val isStreakMode = prefs.getBoolean("isStreakMode", true)
            val labelAbove = prefs.getBoolean("labelAbove", true)

            val bgColor = prefs.getInt("bgColor", android.graphics.Color.BLACK)
            val fgColor = prefs.getInt("fgColor", android.graphics.Color.WHITE)
            val labelColor = prefs.getInt("labelColor", android.graphics.Color.WHITE)

            val todayEpoch = LocalDate.now().toEpochDay()
            val daysElapsed = todayEpoch - startDateEpoch
            val displayValue = if (isStreakMode) daysElapsed + 1 else daysElapsed

            views.setInt(R.id.widget_root, "setBackgroundColor", bgColor)
            views.setTextColor(R.id.widget_number, fgColor)
            views.setTextColor(R.id.widget_label_top, labelColor)
            views.setTextColor(R.id.widget_label_bottom, labelColor)

            views.setTextViewText(R.id.widget_number, displayValue.toString())

            if (labelAbove) {
                views.setViewVisibility(R.id.widget_label_top, View.VISIBLE)
                views.setViewVisibility(R.id.widget_label_bottom, View.GONE)
                views.setTextViewText(R.id.widget_label_top, labelText)
            } else {
                views.setViewVisibility(R.id.widget_label_top, View.GONE)
                views.setViewVisibility(R.id.widget_label_bottom, View.VISIBLE)
                views.setTextViewText(R.id.widget_label_bottom, labelText)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
