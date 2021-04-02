package com.maliotis.newsapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.add
import androidx.fragment.app.commit


class MainActivity : AppCompatActivity() {

    val TAG = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.commit {
            setReorderingAllowed(true)
            add<NewsFragment>(R.id.fragment_container_view)

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (!RealmUtil.realm.isClosed)
            RealmUtil.realm.close()
    }
}