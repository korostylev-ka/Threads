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
    likedByMe = false,
    likes = 0,
    published = ""
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

    //запрос списка постов в фоновом потоке
    fun loadPosts() {
        thread {
            // Начинаем загрузку
            /*используем postValue для записи в livedata с фонового потока, т.к этот метод выполняет
            доставку данных в главный поток*/
            _data.postValue(FeedModel(loading = true))
            try {
                // Данные успешно получены
                val posts = repository.getAll()
                FeedModel(posts = posts, empty = posts.isEmpty())
            } catch (e: IOException) {
                // Получена ошибка
                FeedModel(error = true)
            }.also(_data::postValue)
        }
    }

    fun save() {
        edited.value?.let {
            thread {
                repository.save(it)
                _postCreated.postValue(Unit)
            }
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

    fun likeById(id: Long) {
        val posts = _data.value?.posts?.map {
            if (it.id != id) it else {
                it.copy(
                    likedByMe = !it.likedByMe,
                    likes = (if (!it.likedByMe) ++it.likes else --it.likes)
                )
            }
        }

        thread {
            repository.likeById(id)
            loadPosts()
        }

        //обновляем список постов с поставленным лайком
        _data.postValue(
            posts?.let { _data.value?.copy(posts = posts) }
        )

    }

    fun removeById(id: Long) {
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


    }
}
