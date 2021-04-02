package com.maliotis.newsapp.repository.realm

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class Source: RealmModel {

    @PrimaryKey
    var realmId: String? = null

    var id: String? = null
    var name: String? = null
}