package com.ir0.iptv.app

import android.app.Application
import com.ir0.iptv.app.webpanel.WebPanelServer

class IptvApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        WebPanelServer.start()
    }
}
