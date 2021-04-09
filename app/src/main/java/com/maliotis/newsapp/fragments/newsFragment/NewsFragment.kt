package com.maliotis.newsapp.fragments.newsFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.maliotis.newsapp.R
import com.maliotis.newsapp.enums.ApiStatus
import com.maliotis.newsapp.fragments.adapters.NewsAdapter
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
    lateinit var newsAdapter: NewsAdapter

    lateinit var newsSwipeRefreshLayout: SwipeRefreshLayout

    var state: Int? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        state = savedInstanceState?.getInt("state")

        val view = inflater.inflate(R.layout.news_layout, container, false)

        newsRecyclerView = view.findViewById(R.id.newsRecyclerView)
        newsSwipeRefreshLayout = view.findViewById(R.id.newsSwipeRefreshLayout)
        newsAdapter = NewsAdapter(this)
        val linearLayout = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        newsRecyclerView.adapter = newsAdapter
        newsRecyclerView.layoutManager = linearLayout

        val swipeGestureHelper = SwipeGestureHelper(newsAdapter, newsRecyclerView, newsViewModel)
        val itemTouchHelper = ItemTouchHelper(swipeGestureHelper)
        itemTouchHelper.attachToRecyclerView(newsRecyclerView)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val disp = onScrollObservable(newsRecyclerView)
            .throttleFirst(1000, TimeUnit.MILLISECONDS)
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it) {
                    newsViewModel.getOlderNews()
                }
            }
        disposables.add(disp)

        scrollToPosition(state)
        scrollWhenMovingFirstItem()

        val newsApiDisp = newsViewModel.newsApiSucceeded.subscribe { apiStatus ->
            newsSwipeRefreshLayout.isRefreshing = false
            when (apiStatus) {
                ApiStatus.NOINTERNET -> {
                    // TODO alert user
                    Log.d(TAG, "newsApiSucceeded: NOINTERNET")
                }

                ApiStatus.RATELIMIT -> {
                    // TODO alert user
                    Log.d(TAG, "newsApiSucceeded: RATELIMIT")
                }

                ApiStatus.MAXIMUMRESULTS -> {
                    // TODOalert user
                    Log.d(TAG, "newsApiSucceeded: MAXIMUMRESULTS")
                }

                ApiStatus.FAILED -> {
                    // TODO alert user
                    Log.d(TAG, "newsApiSucceeded: FAILED")
                }

                ApiStatus.SUCCESS -> {
                    // TODO alert user
                    Log.d(TAG, "newsApiSucceeded: SUCCESS")
                }

                // else statement included to make the when statement exhaustive
                else -> {
                    // empty
                 }

            }
        }
        disposables.add(newsApiDisp)

        newsViewModel.getArticles().observe(viewLifecycleOwner, { item ->
            newsAdapter.setData(item)
        })

        newsSwipeRefreshLayout.setOnRefreshListener {
            newsViewModel.getNews()
        }

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

    override fun itemClicked(position: Int, article: Article) {
        newsViewModel.selectItem(article)
    }

    /**
     * Scrolls the recycler view to show the last viewed item in the grid. This is important when
     * navigating back from the grid.
     */
    private fun scrollToPosition(position: Int?) {
        if (position == null) return
        newsAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                val layoutManager: LinearLayoutManager = newsRecyclerView.layoutManager as LinearLayoutManager
                val viewAtPosition = layoutManager.findViewByPosition(position)
                // Scroll to position if the view for the current position is null (not currently part of
                // layout manager children), or it's not completely visible.
                if (viewAtPosition == null || layoutManager
                                .isViewPartiallyVisible(viewAtPosition, false, true)) {
                    newsRecyclerView.post { layoutManager.scrollToPosition(position) }
                }
                newsAdapter.unregisterAdapterDataObserver(this)

            }
        })
    }

    /**
     * Workaround for the issue
     * https://stackoverflow.com/questions/27992427/recyclerview-adapter-notifyitemmoved0-1-scrolls-screen
     */
    private fun scrollWhenMovingFirstItem() {
        newsAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount)
                if (fromPosition == 0) {

                    val layoutManager: LinearLayoutManager =
                        newsRecyclerView.layoutManager as LinearLayoutManager
                    val viewAtPosition = layoutManager.findViewByPosition(toPosition - 1)
                    val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
                    Log.d(TAG, "onItemRangeMoved: firstVisibleItem = $firstVisibleItem")
                    if (viewAtPosition != null && firstVisibleItem == 0) {
                        Log.d(TAG, "onItemRangeMoved: scrolling to position = $fromPosition")
                        layoutManager.scrollToPosition(fromPosition)
                    }
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "onSaveInstanceState: saving state")
        outState.putInt("state", (newsRecyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition())
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
}