package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(): List<Post>
    fun likeById(id: Long, isLiked: Boolean): Post
    fun save(post: Post)
    fun removeById(id: Long)
    //Асинхронные запросы
    //коллбэк по которому результат выволнения придет от репозитория к вью модели
    fun getAllAsync(callback: GetAllCallback)
    fun saveAsync(post: Post, callback: SaveCallback)
    fun removeByIdAsync(id: Long, callback: RemoveByIdCallback)
    fun likeByIdAsync(id: Long, isLiked: Boolean, callback: LikeByIdCallback)

    //интерфейс коллбэка запроса постов
    interface GetAllCallback {
        //вызывается в случае успеха
        fun onSuccess(posts: List<Post>) {}
        //вызыватся в случае неудачи
        fun onError(e: Exception) {}
    }

    //интерфейс коллбэка сохранения
    interface SaveCallback {
        //вызывается в случае успеха
        fun onSuccess(post: Post) {}
        //вызыватся в случае неудачи
        fun onError(e: Exception) {}
    }

    //интерфейс коллбэка сохранения
    interface RemoveByIdCallback {
        //вызывается в случае успеха
        fun onSuccess(posts: List<Post>) {}
        //вызыватся в случае неудачи
        fun onError(e: Exception) {}
    }

    //интерфейс коллбэка лайка
    interface LikeByIdCallback {
        //вызывается в случае успеха
        fun onSuccess(post: Post) {}
        //вызыватся в случае неудачи
        fun onError(e: Exception) {}
    }
}
