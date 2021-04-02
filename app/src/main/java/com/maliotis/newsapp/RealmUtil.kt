package com.maliotis.newsapp

import io.realm.Realm

object RealmUtil {
    val realm: Realm by lazy {
        Realm.getDefaultInstance()
    }
}