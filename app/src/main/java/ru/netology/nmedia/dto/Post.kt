package ru.netology.nmedia.dto

import com.bumptech.glide.Glide
import okhttp3.MediaType

data class Post(
    val id: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: String,
    var likedByMe: Boolean,
    var likes: Int = 0,
    //вложения
    val attachment: Attachment?,
)

//класс вложений
data class Attachment(
    val url: String,
    val description: String,
    val type: String,
)

