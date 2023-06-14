package com.nurhaqhalim.momento.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.nurhaqhalim.momento.R
import com.nurhaqhalim.momento.service.MoWidgetService

class MoWidget : AppWidgetProvider() {
    companion object {
        const val widgetUpdate = "com.nurhaqhalim.momento.WIDGET_UPDATE"

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val intent = Intent(context, MoWidgetService::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            val remoteViews = RemoteViews(context.packageName, R.layout.widget_layout)
            remoteViews.setRemoteAdapter(R.id.sv_widget, intent)
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent?.action != null) {
            if (intent.action == widgetUpdate) {
                val manager = AppWidgetManager.getInstance(context)
                val id = manager.getAppWidgetIds(ComponentName(context, MoWidget::class.java))
                manager.notifyAppWidgetViewDataChanged(id, R.id.sv_widget)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}