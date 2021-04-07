package com.maliotis.newsapp

import android.app.Application
import android.content.Context
import android.util.Log
import com.maliotis.newsapp.repository.realm.Article
import io.realm.*
import io.realm.annotations.RealmModule

class RealmApplication: Application() {

    companion object{
        var staticApplicationContext: Context? = null
    }

    override fun onCreate() {
        super.onCreate()

        Realm.init(this)

        val migration = object: RealmMigration {
            override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
                // DynamicRealm exposes an editable schema
                val schema: RealmSchema = realm.schema
                if (oldVersion == 0L) {
                    schema.get("Article")!!
                            .addField("hidden", Boolean::class.java)
                            .setNullable("hidden", true)

                }
            }

        }

        val config = RealmConfiguration.Builder()
                .addModule(Module())
                .schemaVersion(1) // Must be bumped when the schema changes
                .migration(migration) // Migration to run
                .build()

        Realm.setDefaultConfiguration(config)


        staticApplicationContext = applicationContext
    }

}

@RealmModule(classes = [Article::class] )
open class Module