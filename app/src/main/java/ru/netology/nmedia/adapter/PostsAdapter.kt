package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post

interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onShare(post: Post) {}
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener,
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        val url = "http://10.0.2.2:9999/avatars/${post.authorAvatar}"
        val urlAttachments = "http://10.0.2.2:9999/images/${post.attachment?.url}"
        binding.apply {
            author.text = post.author
            //если поле аватара пустое, устанавливаем значок по умолчанию
            if (post.authorAvatar == "") {
                avatar.setImageResource(R.drawable.ic_no_avatar_24)
            } else {
                //загружаем аватар с помощью Glide
                Glide.with(binding.avatar)
                    .load(url)
                    .placeholder(R.drawable.ic_no_avatar_24)
                    .error(R.drawable.ic_baseline_error_24)
                    .timeout(10_000)
                    .circleCrop() //круглое изображение
                    .into(binding.avatar)
            }
            published.text = post.published
            content.text = post.content
            //если вложений нет, view невидима и не занимает места
            if (post.attachment == null) {
                attachment.isVisible = false
            //если есть вложение
            } else {
                Glide.with(binding.attachment)
                    .load(urlAttachments)
                    .timeout(10_000)
                    .into(binding.attachment)

            }
            // в адаптере
            like.isChecked = post.likedByMe
            like.text = "${post.likes}"

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            like.setOnClickListener {
                onInteractionListener.onLike(post)

            }

            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}
