package com.maliotis.newsapp.fragments.adapters

import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import com.maliotis.newsapp.*
import com.maliotis.newsapp.fragments.ArticleClickListener
import com.maliotis.newsapp.fragments.ArticleDiffCallback
import com.maliotis.newsapp.repository.realm.Article


/**
 * RecyclerView.Adapter for displaying and managing articles for the newsRecycleView
 */
class ArticleAdapter(val listener: ArticleClickListener): RecyclerView.Adapter<ArticleAdapter.ViewHolder>() {

    var listArticles: MutableList<Article> = mutableListOf()

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val imageView: ImageView = itemView.findViewById(R.id.articleImage)
        val pinImageView: ImageView = itemView.findViewById(R.id.articlePin)
        val title: TextView = itemView.findViewById(R.id.articleTitle)
        val publishedAt: TextView = itemView.findViewById(R.id.articlePublishedDate)
        val description: TextView = itemView.findViewById(R.id.articleDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.article_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.isNotEmpty()) {
            Log.d("TAG", "onBindViewHolder: payloads")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = listArticles[position]

        holder.imageView.load(article.urlToImage, false) {
            onLoadCompleted(position)
            // possibly save image to db
        }
        holder.title.text = article.title
        holder.imageView.transitionName = article.urlToImage
        holder.publishedAt.text = isoToDate(article.publishedAt)
        holder.description.text = Html.fromHtml(article.description)
        holder.pinImageView.alpha = if (article.pinned == true) 1f else 0f

        holder.itemView.setOnClickListener {
            listener.itemClicked(position, listArticles[position])
        }
    }

    fun setData(newArticles: List<Article>) {
        val diffCallback = ArticleDiffCallback(listArticles, newArticles)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        listArticles.clear()
        listArticles.addAll(newArticles)
        diffResult.dispatchUpdatesTo(this)
    }

    private fun onLoadCompleted(position: Int) {

    }

    override fun getItemCount(): Int {
        return listArticles.size
    }

}