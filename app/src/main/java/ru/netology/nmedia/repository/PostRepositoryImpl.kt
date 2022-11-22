package ru.netology.nmedia.repository

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.dto.Post
import java.util.concurrent.TimeUnit


class PostRepositoryImpl: PostRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    //для получения списка постов
    private val typeToken = object : TypeToken<List<Post>>() {}

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999"
        private val jsonType = "application/json".toMediaType()
    }

    override fun getAll(): List<Post> {
        // запрос
        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts")
            .build()
        //возвращает ответ на запрос
        return client.newCall(request)
            .execute()
            //тело ответа
            .let { it.body?.string() ?: throw RuntimeException("body is null") }
            //"распарсим"
            .let {
                gson.fromJson(it, typeToken.type)
            }
    }
    //запрос поста по id
    fun getPost(id: Long): Post{
        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts/$id")
            .build()
        //возвращает ответ на запрос
        return client.newCall(request)
            .execute()
            //тело ответа
            .let { it.body?.string() ?: throw RuntimeException("body is null") }
            //"распарсим"
            .let {
                gson.fromJson(it, Post::class.java)
            }
    }

    override fun likeById(id: Long): Post {
        //получаем пост с id
        val post = getPost(id)

        //если лайк поставлен, то удаляем лайк
        if (post.likedByMe) {
            val request: Request = Request.Builder()
                //delete запрос
                .delete()
                .url("${BASE_URL}/api/slow/posts/$id/likes")
                .build()

            return client.newCall(request)
                .execute()
                .let { it.body?.string() ?: throw RuntimeException("body is null") }
                .let {
                    gson.fromJson(it, Post::class.java)
                }
        } else {
            //если лайк не поcтавлен, ставим его
            val request: Request = Request.Builder()
                //post
                .post(gson.toJson(post).toRequestBody(jsonType))
                .url("${BASE_URL}/api/slow/posts/$id/likes")
                .build()

            return client.newCall(request)
                .execute()
                .let { it.body?.string() ?: throw RuntimeException("body is null") }
                .let {
                    gson.fromJson(it, Post::class.java)
                }
        }
    }

    override fun save(post: Post) {
        val request: Request = Request.Builder()
             //post запрос
            .post(gson.toJson(post).toRequestBody(jsonType))
            .url("${BASE_URL}/api/slow/posts")
            .build()

        client.newCall(request)
            .execute()
            .close()
    }

    override fun removeById(id: Long) {
        val request: Request = Request.Builder()
            //delete запрос
            .delete()
            .url("${BASE_URL}/api/slow/posts/$id")
            .build()

        client.newCall(request)
            .execute()
            .close()
    }
}
