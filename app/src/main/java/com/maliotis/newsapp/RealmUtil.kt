package com.maliotis.newsapp

import io.realm.Realm

object RealmUtil {

    var realm: Realm = Realm.getDefaultInstance()
}