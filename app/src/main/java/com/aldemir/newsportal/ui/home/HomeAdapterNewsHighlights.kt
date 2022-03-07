package com.aldemir.newsportal.ui.home

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aldemir.newsportal.R
import com.aldemir.newsportal.databinding.ItemNewsHighlightsBinding
import com.aldemir.newsportal.models.New
import com.bumptech.glide.Glide
import java.io.FileNotFoundException


class HomeAdapterNewsHighlights(private var users: List<New>) :
    RecyclerView.Adapter<HomeAdapterNewsHighlights.DataViewHolder>() {

    companion object {
        const val TAG = "HomeAdapterNewsHighlights"
    }

    lateinit var mClickListener: ClickListener

    fun setOnItemClickListener(aClickListener: ClickListener) {
        mClickListener = aClickListener
    }

    interface ClickListener {
        fun onClickCarousel(position: Int, aView: View)
    }

    inner class DataViewHolder(private val itemBinding: ItemNewsHighlightsBinding) :
        RecyclerView.ViewHolder(itemBinding.root),
        View.OnClickListener {
        fun bind(new: New) {
            itemBinding.textViewTitleHighlights.text = new.title.substring(0, 40)
            try {
                Glide.with(itemBinding.imageViewHighlights.context)
                    .load(new.image_url)
                    .placeholder(R.drawable.loading_image)
                    .error(R.drawable.image_not_found)
                    .into(itemBinding.imageViewHighlights)
            } catch (err: FileNotFoundException) {
                Log.e(TAG, err.message.toString())
            }
        }

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            mClickListener.onClickCarousel(adapterPosition, v)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        DataViewHolder(
            ItemNewsHighlightsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) =
        holder.bind(users[position])

    fun addData(list: List<New>) {
        users = list
        notifyDataSetChanged()
    }

}