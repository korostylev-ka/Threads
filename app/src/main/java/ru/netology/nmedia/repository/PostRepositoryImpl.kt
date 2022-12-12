package ru.netology.nmedia.repository

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.activity.AppActivity
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dto.Post
import java.io.IOException
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

    //синхронный запролс постов
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

    //запрос списка постов через retrofit
    override fun getAllAsync(callback: PostRepository.GetAllCallback) {
        PostsApi.retrofitService.getAll().enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                try {
                    //если ответ неуспешный (не 2ХХ)
                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException(response.message()))
                        return
                    }
                    //ответ успешный 2хх
                    callback.onSuccess(response.body() ?: throw RuntimeException("body is null"))
                } catch (e: Exception) {
                    callback.onError(e)
                }
            }
            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                callback.onError(Exception(t))
            }
        })

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

    //синхронный запрос на лайк/дизлайк
    override fun likeById(id: Long, isLiked: Boolean): Post {
        //если лайк поставлен, то удаляем лайк
        if (isLiked == true) {
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
                .post(gson.toJson(id).toRequestBody(jsonType))
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

    //выставление/снятие лайка через retrofit
    override fun likeByIdAsync(
        id: Long,
        isLiked: Boolean,
        callback: PostRepository.LikeByIdCallback
    ) {
        //дизлвайк
        if (isLiked == true) {
            PostsApi.retrofitService.dislikeById(id).enqueue(object : Callback<Post> {
                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    try {
                        //если ответ неуспешный (не 2ХХ)
                        if (!response.isSuccessful) {
                            callback.onError(RuntimeException(response.message()))
                            return
                        }
                        //ответ успешный 2хх
                        callback.onSuccess(response.body() ?: throw RuntimeException("body is null"))
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }

                override fun onFailure(call: Call<Post>, t: Throwable) {
                    callback.onError(Exception(t))
                }
            })
            //лайк
        } else {
            PostsApi.retrofitService.likeById(id).enqueue(object : Callback<Post> {
                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    try {
                        //если ответ неуспешный (не 2ХХ)
                        if (!response.isSuccessful) {
                            callback.onError(RuntimeException(response.message()))
                            return
                        }
                        //ответ успешный 2хх
                        callback.onSuccess(response.body() ?: throw RuntimeException("body is null"))
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }

                override fun onFailure(call: Call<Post>, t: Throwable) {
                    callback.onError(Exception(t))
                }
            })
        }
    }

    //синхронное сохранение поста
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

    //сохранение поста через retrofit
    override fun saveAsync(post: Post, callback: PostRepository.SaveCallback) {
        PostsApi.retrofitService.save(post).enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                try {
                    //если ответ неуспешный (не 2ХХ)
                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException(response.message()))
                        //Toast.makeText(, "Error",Toast.LENGTH_SHORT)
                        return
                    }
                    //ответ успешный 2хх
                    callback.onSuccess(response.body() ?: throw RuntimeException("body is null"))
                } catch (e: Exception) {
                    callback.onError(e)
                }
            }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                callback.onError(Exception(t))
            }
        })
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
    //удаление поста через retrofit
    override fun removeByIdAsync(id: Long, callback: PostRepository.RemoveByIdCallback) {
        PostsApi.retrofitService.removeById(id).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                try {
                    //если ответ неуспешный (не 2ХХ)
                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException(response.message()))
                        return
                    }
                    //ответ успешный, ничего не возвращаем
                } catch (e: Exception) {
                    callback.onError(e)
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                callback.onError(Exception(t))
            }
        })

    }
}





