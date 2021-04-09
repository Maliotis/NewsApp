package com.maliotis.newsapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.maliotis.newsapp.fragments.articleFragment.ArticleFragment
import com.maliotis.newsapp.fragments.newsFragment.NewsFragment
import com.maliotis.newsapp.viewModels.NewsViewModel


class MainActivity : AppCompatActivity() {

    val TAG = MainActivity::class.java.simpleName

    val viewModel: NewsViewModel by viewModels()
    var orientationChangeFirstTime = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<NewsFragment>(R.id.fragment_container_view)

            }
        } else {
            orientationChangeFirstTime = true
        }

        viewModel.selectedItem.observe(this, { item ->
            if (orientationChangeFirstTime) {
                orientationChangeFirstTime = false
                return@observe
            }
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<ArticleFragment>(R.id.fragment_container_view)
                addToBackStack(null)
            }
            viewModel.addDetailArticle(item)
        })

    }
}