package com.maliotis.newsapp.fragments

import android.graphics.Canvas
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.maliotis.newsapp.R
import com.maliotis.newsapp.fragments.adapters.ArticleAdapter
import com.maliotis.newsapp.viewModels.NewsViewModel
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

class SwipeGestureHelper(val articleAdapter: ArticleAdapter,
                         val recyclerView: RecyclerView,
                         val viewModel: NewsViewModel):
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

        val position = viewHolder.adapterPosition

        when(direction) {
            ItemTouchHelper.LEFT -> {
                val article = articleAdapter.listArticles[position]
                val articleId = article.id
                viewModel.hideArticle(articleId)

                Snackbar.make(recyclerView, "", Snackbar.LENGTH_LONG)
                        .setAction("Undo") {
                            viewModel.hideArticle(articleId, false)
                        }.show()
            }

            ItemTouchHelper.RIGHT -> {

            }
        }
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                .addSwipeLeftActionIcon(R.drawable.ic_delete_black_24dp)
                .addSwipeLeftBackgroundColor(Color.rgb(200, 100, 100))
                .create()
                .decorate()

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}