package com.maliotis.newsapp

import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.google.android.material.card.MaterialCardView
import com.makeramen.roundedimageview.RoundedImageView
import com.maliotis.newsapp.enums.ItemSelected
import com.maliotis.newsapp.fragments.ArticleFragment
import com.maliotis.newsapp.fragments.NewsFragment
import com.maliotis.newsapp.repository.realm.Article
import com.maliotis.newsapp.viewModels.NewsViewModel


class MainActivity : AppCompatActivity() {

    val TAG = MainActivity::class.java.simpleName

    val viewModel: NewsViewModel by viewModels()

    companion object {
        var currentPosition = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.commit {
            setReorderingAllowed(true)
            add<NewsFragment>(R.id.fragment_container_view)

        }

        viewModel.selectedItem.observe(this, { item ->
            when (item.first) {

                ItemSelected.ARTICLEITEM -> {
                    val pairValue = (item.second as? Pair<*, *>) ?: return@observe
                    val article = (pairValue.first as? Article) ?: return@observe
                    val sharedViews = (pairValue.second as? List<*>) ?: return@observe

                    supportFragmentManager.commit {
                        val imageView = (sharedViews[0] as? RoundedImageView) ?: return@observe
                        setReorderingAllowed(true)
                        addSharedElement(imageView, "article_imageView")
                        replace<ArticleFragment>(R.id.fragment_container_view)
                    }

                    viewModel.addDetailArticle(article)
                }

                ItemSelected.ARTICLEBACK -> {
                    val sharedViews = (item.second as? List<*>) ?: return@observe

                    supportFragmentManager.commit {
                        val imageView = (sharedViews[0] as? RoundedImageView) ?: return@observe
                        setReorderingAllowed(true)
                        addSharedElement(imageView, "item_imageView")
                        replace<NewsFragment>(R.id.fragment_container_view)
                    }
                }
            }



        })

    }

    override fun onDestroy() {
        super.onDestroy()
        if (!RealmUtil.realm.isClosed)
            RealmUtil.realm.close()
    }
}