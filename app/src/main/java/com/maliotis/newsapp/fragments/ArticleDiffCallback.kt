package com.maliotis.newsapp.fragments

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

    override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        if (!oldList[oldPosition].isValid()) {
            return false
        }
        val hidden = oldList[oldPosition].pinned ?: false
        val hidden1 = newList[newPosition].pinned ?: false

        if (hidden != hidden1) {
            Log.d("TAG", "areContentsTheSame: not the same")
        }

        return hidden == hidden1
    }

    override fun getChangePayload(oldPosition: Int, newPosition: Int): Any? {
        return super.getChangePayload(oldPosition, newPosition)
    }
}