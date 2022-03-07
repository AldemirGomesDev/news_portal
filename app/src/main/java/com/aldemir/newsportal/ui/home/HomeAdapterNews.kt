package com.aldemir.newsportal.ui.home

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aldemir.newsportal.MyApplication
import com.aldemir.newsportal.R
import com.aldemir.newsportal.databinding.ItemNewsBinding
import com.aldemir.newsportal.models.New
import com.bumptech.glide.Glide
import java.lang.Exception


class HomeAdapterNews : ListAdapter<New, HomeAdapterNews.ViewHolder>(CoinItemCallback) {

    companion object {
        const val TAG = "HomeAdapterNews"
    }

    lateinit var mClickListener: ClickListener
    lateinit var mClickListenerFavorite: ClickListener
    lateinit var mClickListenerShared: ClickListener

    fun setOnItemClickListener(aClickListener: ClickListener) {
        mClickListener = aClickListener
    }

    fun setOnItemClickListenerFavorite(aClickListener: ClickListener) {
        mClickListenerFavorite = aClickListener
    }

    fun setOnItemClickListenerShared(aClickListener: ClickListener) {
        mClickListenerShared = aClickListener
    }

    interface ClickListener {
        fun onClickNew(position: Int, aView: View)
        fun onClickFavorite(position: Int, aView: View)
        fun onClickShared(position: Int, aView: View)
    }

    object CoinItemCallback : DiffUtil.ItemCallback<New>() {
        override fun areItemsTheSame(oldItem: New, newItem: New): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: New, newItem: New): Boolean = oldItem == newItem
    }

    private var coins: List<New> = emptyList()
        set(value) {
            field = value
            onListOrFilterChange()
        }

    private var filter: CharSequence = ""
        set(value) {
            field = value
            onListOrFilterChange()
        }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ItemNewsBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val new = currentList[position]
        holder.binding.textViewTitle.text = new.title
        holder.binding.textViewPublishedAt.text = new.description
        try {
            Glide.with(holder.binding.imageViewNew.context)
                .load(new.image_url)
                .placeholder(R.drawable.loading_image)
                .error(R.drawable.image_not_found)
                .into(holder.binding.imageViewNew)
        } catch (err: Exception) {
            Log.e(TAG, err.message.toString())
        }

        if (new.is_favorite) {
            holder.binding.imageFavorite.setColorFilter(
                ContextCompat
                    .getColor(MyApplication.appContext, R.color.colorPrimary)
            )
            holder.binding.textFavorite.setTextColor(
                ContextCompat
                    .getColor(MyApplication.appContext, R.color.colorPrimary)
            )
            holder.binding.textFavorite.text =
                MyApplication.appContext.getString(R.string.not_favorite)
        } else {
            holder.binding.imageFavorite.setColorFilter(
                ContextCompat
                    .getColor(MyApplication.appContext, R.color.colorAccent)
            )
            holder.binding.textFavorite.setTextColor(
                ContextCompat
                    .getColor(MyApplication.appContext, R.color.colorAccent)
            )
            holder.binding.textFavorite.text = MyApplication.appContext.getString(R.string.favorite)
        }
    }

    private fun onListOrFilterChange() {
        if (filter.length < 2) {
            submitList(coins)
            return
        }
        val pattern = filter.toString().lowercase().trim()
        val filteredList = coins.filter { pattern in it.title.lowercase() }
        submitList(filteredList)
    }

    inner class ViewHolder(val binding: ItemNewsBinding) : RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {
        override fun onClick(v: View) {
            mClickListener.onClickNew(adapterPosition, v)
        }

        init {
            binding.root.setOnClickListener(this)
            binding.buttonFavorite.setOnClickListener {
                mClickListenerFavorite.onClickFavorite(adapterPosition, it)
            }
            binding.buttonShared.setOnClickListener {
                mClickListenerShared.onClickShared(adapterPosition, it)
            }
        }
    }
}