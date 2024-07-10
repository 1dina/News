package com.example.news.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.news.databinding.ItemArticlePreviewBinding
import com.example.news.models.Article

class NewsAdapter : RecyclerView.Adapter<NewsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemArticlePreviewBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val differCallback =
        object : DiffUtil.ItemCallback<Article>() {
            override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
                return oldItem.url == newItem.url
            }

            override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
                return oldItem == newItem
            }

        }
    val differ = AsyncListDiffer(this, differCallback)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemArticlePreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = differ.currentList[position]
        holder.binding.apply {
            tvTitle.text = article.title
            tvSource.text = article.source?.name
            tvDescription.text = article.description
            tvPublishedAt.text = article.publishedAt
            Glide.with(holder.itemView).load(article.urlToImage).into(ivArticleImage)
        }
        holder.itemView.setOnClickListener {
            onItemSelected?.let { it(article) }
        }


    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private var onItemSelected: ((Article) -> Unit)? = null
    fun setOnItemSelected(listener: (Article) -> Unit) {
        onItemSelected = listener
    }
}