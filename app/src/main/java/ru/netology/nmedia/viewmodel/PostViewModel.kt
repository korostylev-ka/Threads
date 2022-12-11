package ru.netology.nmedia.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.*
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.AppActivity
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

    //сохраняем асинхронно
    fun save() {
        edited.value?.let {
           repository.saveAsync(it, object : PostRepository.SaveCallback{
               override fun onSuccess(post: Post) {
                   _postCreated.postValue(Unit)
               }

               override fun onError(e: Exception) {
                   _data.postValue(FeedModel(error = true))
                   //при ошибке создаем всплывающее состояние. Даем возможность еще раз сохранить
                   Toast.makeText(getApplication(),R.string.error_loading,Toast.LENGTH_LONG)
                       .show()
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


    fun likeById(id: Long, isLiked: Boolean) {
        val old = _data.value?.posts.orEmpty()
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
                //создаем уведомление об ошибке конкретного поста
                Toast.makeText(getApplication(), R.string.error_loading_post, Toast.LENGTH_LONG)
                    .show()
                _data.postValue(
                    _data.value?.copy(posts = old)
                    )
                //загружаем заново все посты, если ошибка будет "глобальная", тогда выйдет ошибка и кнопка RETRY
                loadPosts()
                /*убираем
                data.postValue(FeedModel(error = true))
                чтобы не выдавалась общая ошибка по всем постам
                 */
            }
        })
    }

    //удаляем асинхронно
    fun removeById(id: Long) {
        val old = _data.value?.posts.orEmpty()
        _data.postValue(
            _data.value?.copy(posts = _data.value?.posts.orEmpty()
                .filter { it.id != id }
            )
        )
        repository.removeByIdAsync(id, object : PostRepository.RemoveByIdCallback{
            override fun onSuccess(unit: Unit) {
                //_data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))

            }
            override fun onError(e: Exception) {
                //сообщаем об ошибке
                Toast.makeText(getApplication(), R.string.error_loading, Toast.LENGTH_LONG)
                    .show()
                _data.postValue(FeedModel(error = true))
                _data.postValue(_data.value?.copy(posts = old)
                )
                //загружаем заново список постов, включая удаленный
                loadPosts()
            }
        })
    }

}
