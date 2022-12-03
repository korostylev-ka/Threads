package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.IOException
import kotlin.concurrent.thread

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    authorAvatar = "",
    likedByMe = false,
    likes = 0,
    published = "",
    attachment = null

)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

    //запрос списка постов ачинхронно
    fun loadPosts() {
        // Начинаем загрузку
        /*используем postValue для записи в livedata с фонового потока, т.к этот метод выполняет
        доставку данных в главный поток*/
        _data.postValue(FeedModel(loading = true))
        //метод асинхронный, передаем анонимный объект, реализующий интерфейс
        repository.getAllAsync(object : PostRepository.GetAllCallback{
            override fun onSuccess(posts: List<Post>) {
                //выполняется в фоновом потоке
                _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

    /*fun save() {
        edited.value?.let {
            thread {
                repository.save(it)
                _postCreated.postValue(Unit)
            }
        }
        edited.value = empty
    }*/

    //сохраняем асинхронно
    fun save() {
        edited.value?.let {
           repository.saveAsync(it, object : PostRepository.SaveCallback{
               override fun onSuccess(post: Post) {
                   _postCreated.postValue(Unit)
               }

               override fun onError(e: Exception) {
                   _data.postValue(FeedModel(error = true))
               }
           })
        }
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    /*fun likeById(id: Long, isLiked: Boolean) {
        thread {
            //получаем пост с измененным количеством лайков
           val postLiked = repository.likeById(id, isLiked)
            //изменяем пост в списке постов, меняя поле likedByMe
            val posts = _data.value?.posts?.map {
                if (it.id == id) {
                    postLiked.copy(likedByMe = !it.likedByMe)
                } else it
            }
            _data.postValue(
                posts?.let { _data.value?.copy(posts = posts) }
            )
        }
    }*/

    fun likeById(id: Long, isLiked: Boolean) {
        repository.likeByIdAsync(id, isLiked, object : PostRepository.LikeByIdCallback{
            override fun onSuccess(post: Post) {
                val posts = _data.value?.posts?.map {
                    if (it.id == id) {
                        post.copy(likedByMe = !it.likedByMe)
                    } else it
                }
                _data.postValue(
                    posts?.let { _data.value?.copy(posts = posts) }
                )
            }
            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))


            }
        })

    }
    /*fun removeById(id: Long) {
        thread {
            // Оптимистичная модель
            //сохраняем старый список постов
            val old = _data.value?.posts.orEmpty()
            _data.postValue(
                _data.value?.copy(posts = _data.value?.posts.orEmpty()
                    .filter { it.id != id }
                )
            )
            try {
                //пробуем удалить на сервере
                repository.removeById(id)
            } catch (e: IOException) {
                //если что-то пошло не так, "откатываемся" к предыдущему состоянию
                _data.postValue(_data.value?.copy(posts = old))
            }
        }
    }*/

    //удаляем асинхронно
    fun removeById(id: Long) {
        val old = _data.value?.posts.orEmpty()
        _data.postValue(
            _data.value?.copy(posts = _data.value?.posts.orEmpty()
                .filter { it.id != id }
            )
        )
        repository.removeByIdAsync(id, object : PostRepository.RemoveByIdCallback{
            override fun onSuccess(posts: List<Post>) {
                _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))

            }
            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
                _data.postValue(_data.value?.copy(posts = old)
                )
            }
        })
    }

}
