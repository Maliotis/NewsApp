package com.maliotis.newsapp.fragments.newsFragment

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.maliotis.newsapp.repository.realm.Article
import io.realm.kotlin.isValid

/**
 * Implementation of the DiffUtil.Callback to detect insertions/deletions and updates for the
 * newsRecyclerView
 */
class ArticleDiffCallback(private val oldList: List<Article>, private val newList: List<Article>) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        if (!oldList[oldItemPosition].isValid()) {
            return false
        }
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    /**
     * Compare only the [Article.pinned] attribute as that's the only thing we update
     * Note: the [Article.hidden] attribute when changed to true is not part of the recyclerView
     * therefore no need to compare
     */
    override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        if (!oldList[oldPosition].isValid()) {
            return false
        }
        val hidden = oldList[oldPosition].pinned ?: false
        val hidden1 = newList[newPosition].pinned ?: false

        return hidden == hidden1
    }

}