package com.maliotis.newsapp.fragments

import android.os.Bundle
import android.transition.TransitionInflater
import android.transition.TransitionSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import androidx.core.app.SharedElementCallback
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.maliotis.newsapp.MainActivity
import com.maliotis.newsapp.R
import com.maliotis.newsapp.convertDPToPixels
import com.maliotis.newsapp.enums.ApiStatus
import com.maliotis.newsapp.enums.ItemSelected
import com.maliotis.newsapp.fragments.adapters.ArticleAdapter
import com.maliotis.newsapp.fragments.itemDecorators.GridSpacingItemDecoration
import com.maliotis.newsapp.repository.realm.Article
import com.maliotis.newsapp.viewModels.NewsViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit


class NewsFragment: Fragment(), ArticleClickListener {

    val TAG = NewsFragment::class.java.simpleName

    private val newsViewModel: NewsViewModel by activityViewModels()
    private var disposables = CompositeDisposable()

    lateinit var newsRecyclerView: RecyclerView
    lateinit var articleAdapter: ArticleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = TransitionInflater.from(requireContext())
            .inflateTransition(R.transition.shared_image)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.news_layout, container, false)

        postponeEnterTransition()
        newsRecyclerView = view.findViewById(R.id.newsRecyclerView)
        articleAdapter = ArticleAdapter(this)
        val gridLayout = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        val spacing = convertDPToPixels(24f, requireContext()).toInt()
        val decorator = GridSpacingItemDecoration(2, spacing, true)
        newsRecyclerView.adapter = articleAdapter
        newsRecyclerView.layoutManager = gridLayout
        newsRecyclerView.addItemDecoration(decorator)

        //prepareTransitions()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scrollToPosition()

        val disp = onScrollObservable(newsRecyclerView)
            .debounce(1000, TimeUnit.MILLISECONDS)
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it) {
                    newsViewModel.getOlderNews()
                }
            }
        disposables.add(disp)

        val newsApiDisp = newsViewModel.newsApiSucceeded.subscribe { apiStatus ->
            when (apiStatus) {
                ApiStatus.NOINTERNET -> {
                    // alert user and show cached-data
                    Log.d(TAG, "newsApiSucceeded: NOINTERNET")
                }

                ApiStatus.RATELIMIT -> {
                    // alert user with animation
                    Log.d(TAG, "newsApiSucceeded: RATELIMIT")
                }

                ApiStatus.MAXIMUMRESULTS -> {
                    Log.d(TAG, "newsApiSucceeded: MAXIMUMRESULTS")
                }

                ApiStatus.FAILED -> {
                    // alert user with animation
                    Log.d(TAG, "newsApiSucceeded: FAILED")
                }

                ApiStatus.SUCCESS -> {
                    Log.d(TAG, "newsApiSucceeded: SUCCESS")
                }
            }
        }
        disposables.add(newsApiDisp)

        newsViewModel.getArticles().observe(viewLifecycleOwner, { item ->
            val start = articleAdapter.listArticles.size
            articleAdapter.listArticles.addAll(item.toMutableList())
            articleAdapter.notifyItemRangeInserted(start, item.size)

            (view.parent as? ViewGroup)?.doOnPreDraw {
                //setSharedElement()
//                startPostponedEnterTransition()
            }
        })


    }

    private fun onScrollObservable(recyclerView: RecyclerView): Observable<Boolean> {
        return Observable.create { emitter ->
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (!recyclerView.canScrollVertically(1)) {
                        emitter.onNext(true)
                    }
                }
            })
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    override fun itemClicked(position: Int, article: Article, shareViewElements: List<View>) {
        MainActivity.currentPosition = position
        ViewCompat.setTransitionName(shareViewElements[0], "item_imageView")

        val pairValue = Pair(article, shareViewElements)
        val pairItem = Pair<ItemSelected, Any>(ItemSelected.ARTICLEITEM, pairValue)

//        (exitTransition as TransitionSet).excludeTarget(shareViewElements[0].parent as ViewGroup, true)


        newsViewModel.selectItem(pairItem)
    }

    /**
     * Scrolls the recycler view to show the last viewed item in the grid. This is important when
     * navigating back from the grid.
     */
    private fun scrollToPosition() {
        newsRecyclerView.addOnLayoutChangeListener(object : OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                newsRecyclerView.removeOnLayoutChangeListener(this)
                val layoutManager: RecyclerView.LayoutManager? = newsRecyclerView.layoutManager
                val viewAtPosition = layoutManager?.findViewByPosition(MainActivity.currentPosition)
                // Scroll to position if the view for the current position is null (not currently part of
                // layout manager children), or it's not completely visible.
                if (viewAtPosition == null ||
                    layoutManager.isViewPartiallyVisible(viewAtPosition, false, true)
                ) {
                    newsRecyclerView.post { layoutManager?.scrollToPosition(MainActivity.currentPosition) }
                }
            }
        })
    }

    private fun setSharedElement() {

        val selectedViewHolder: ArticleAdapter.ViewHolder = (newsRecyclerView
            .findViewHolderForAdapterPosition(MainActivity.currentPosition) ?: return) as ArticleAdapter.ViewHolder

        val imageView = selectedViewHolder.imageView


        ViewCompat.setTransitionName(imageView, "item_imageView")

    }

    /**
     * Prepares the shared element transition to the pager fragment, as well as the other transitions
     * that affect the flow.
     */
    private fun prepareTransitions() {
        exitTransition = TransitionInflater.from(context)
            .inflateTransition(R.transition.shared_image)

        // A similar mapping is set at the ImagePagerFragment with a setEnterSharedElementCallback.
        setExitSharedElementCallback(
            object : SharedElementCallback() {
                override fun onMapSharedElements(
                    names: List<String?>,
                    sharedElements: MutableMap<String?, View?>
                ) {
                    // Locate the ViewHolder for the clicked position.
                    val selectedViewHolder: RecyclerView.ViewHolder = newsRecyclerView
                        .findViewHolderForAdapterPosition(MainActivity.currentPosition) ?: return

                    // Map the first shared element name to the child ImageView.
                    sharedElements[names[0]] = selectedViewHolder.itemView.findViewById(R.id.articleImage)
                    sharedElements[names[1]] = selectedViewHolder.itemView.findViewById(R.id.articleTitle)
                }
            })
    }


}