package com.nurhaqhalim.momento.service

import android.content.Intent
import android.widget.RemoteViewsService

class MoWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(p0: Intent?): RemoteViewsFactory {
        return MoViewsFactory(this.applicationContext)
    }
}