package com.maliotis.newsapp.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.app.SharedElementCallback
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListenerAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.card.MaterialCardView
import com.makeramen.roundedimageview.RoundedImageView
import com.maliotis.newsapp.R
import com.maliotis.newsapp.convertDPToPixels
import com.maliotis.newsapp.enums.ItemSelected
import com.maliotis.newsapp.getClickObservable
import com.maliotis.newsapp.isoToDate
import com.maliotis.newsapp.viewModels.NewsViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit


class ArticleFragment: Fragment() {

    val TAG = ArticleFragment::class.java.simpleName

    val viewModel: NewsViewModel by activityViewModels()
    val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(requireContext())
            .inflateTransition(R.transition.shared_image)

        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: List<String?>?,
                sharedElements: Map<String?, View?>
            ) {
                super.onMapSharedElements(names, sharedElements)
                

                Log.d(TAG, "onMapSharedElements: ")
            }
        })


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.article_layout, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        postponeEnterTransition()
        val articleBackButton = view.findViewById<ImageButton>(R.id.articleBackButton)
        val scrollView = view.findViewById<ScrollView>(R.id.articleScrollView)
        val imageView = scrollView.findViewById<RoundedImageView>(R.id.articleLayoutImage)
        val titleView = scrollView.findViewById<TextView>(R.id.articleLayoutTitle)
        val contentView = scrollView.findViewById<TextView>(R.id.articleContent)
        val publishedAtView = scrollView.findViewById<TextView>(R.id.publishedDate)

        articleBackButtonObserver(articleBackButton)

        ViewCompat.setTransitionName(imageView, "article_imageView")

        viewModel.detailArticle.observe(viewLifecycleOwner, {
            titleView.text = it.title
            contentView.text = it.description
            publishedAtView.text = isoToDate(it.publishedAt)
            Glide.with(requireContext())
                .load(it.urlToImage)
//                .transform(RoundedCorners(18))
//                .centerCrop()
//                .apply(
//                    RequestOptions().dontTransform() // this line
//                )
                .listener(object : RequestListener<Drawable> {

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        startPostponedEnterTransition()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        startPostponedEnterTransition()
                        return false
                    }
                })

                .into(imageView)
        })

    }

    private fun articleBackButtonObserver(backButton: ImageButton) {
        val backDisp = backButton.getClickObservable()
            .debounce(300, TimeUnit.MILLISECONDS)
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Log.d(
                    TAG,
                    "articleBackButtonObserver: thread.name = ${Thread.currentThread().name}"
                )
                val view = it.parent as ViewGroup
                val scrollView = view.findViewById<ScrollView>(R.id.articleScrollView)
                val imageView = scrollView.findViewById<RoundedImageView>(R.id.articleLayoutImage)

                val sharedViewsList = listOf<View>(imageView)
                val pair = Pair(ItemSelected.ARTICLEBACK, sharedViewsList)
                viewModel.selectItem(pair)
            }

        disposables.add(backDisp)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
}