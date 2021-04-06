package com.maliotis.newsapp.fragments.adapters

import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.card.MaterialCardView
import com.makeramen.roundedimageview.RoundedImageView
import com.maliotis.newsapp.MainActivity
import com.maliotis.newsapp.R
import com.maliotis.newsapp.fragments.ArticleClickListener
import com.maliotis.newsapp.fragments.NewsFragment
import com.maliotis.newsapp.repository.realm.Article

class ArticleAdapter(val listener: ArticleClickListener): RecyclerView.Adapter<ArticleAdapter.ViewHolder>() {

    var listArticles: MutableList<Article> = mutableListOf()

    class ViewHolder(val view: View): RecyclerView.ViewHolder(view) {

        val imageView: RoundedImageView = itemView.findViewById(R.id.articleImage)
        val title: TextView = itemView.findViewById(R.id.articleTitle)

        fun bind() {
            ViewCompat.setTransitionName(imageView, "item_imageView")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.article_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == MainActivity.currentPosition) {
            holder.bind()
            (listener as NewsFragment).startPostponedEnterTransition()
        }
        //holder.bind()

        Glide.with(holder.view.context)
            .load(listArticles?.get(position)?.urlToImage)
//            .transform(RoundedCorners(18))
//            .centerCrop()
//            .apply(
//                RequestOptions().dontTransform() // this line
//            )
            .into(holder.imageView)

        holder.title.text = listArticles?.get(position)?.title

        holder.itemView.setOnClickListener {
            val imageView = it.findViewById<RoundedImageView>(R.id.articleImage)
            val sharedElements = listOf<View>(imageView, )
            listener.itemClicked(position, listArticles[position], sharedElements)
        }
    }

    override fun getItemCount(): Int {
        return listArticles?.size ?: 0
    }

    override fun getItemId(position: Int): Long {
        return listArticles[position].id.hashCode().toLong()
    }
}