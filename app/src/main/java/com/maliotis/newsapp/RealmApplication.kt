package com.maliotis.newsapp

import android.app.Application
import android.content.Context
import io.realm.Realm

class RealmApplication: Application() {

    companion object{
        var staticApplicationContext: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        staticApplicationContext = applicationContext
    }
}