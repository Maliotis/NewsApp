package com.maliotis.newsapp.fragments.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.maliotis.newsapp.R
import com.maliotis.newsapp.repository.realm.Article

class ArticleAdapter: RecyclerView.Adapter<ArticleAdapter.ViewHolder>() {

    var listArticles: MutableList<Article>? = null

    class ViewHolder(val view: View): RecyclerView.ViewHolder(view) {

        val imageView: ImageView = itemView.findViewById(R.id.articleImage)
        val title: TextView = itemView.findViewById(R.id.articleTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.article_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var requestOptions = RequestOptions()
        requestOptions = requestOptions.transform(CenterCrop(), RoundedCorners(18))
        Glide.with(holder.view.context)
            .load(listArticles?.get(position)?.urlToImage)
            .apply(requestOptions)
            .into(holder.imageView)

        holder.title.text = listArticles?.get(position)?.title
    }

    override fun getItemCount(): Int {
        return listArticles?.size ?: 0
    }
}