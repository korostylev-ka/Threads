package ru.netology.nmedia.model

import ru.netology.nmedia.dto.Post

//различные состояния UI
data class FeedModel(
    val posts: List<Post> = emptyList(),
    //загрузка
    val loading: Boolean = false,
    //ошибка
    val error: Boolean = false,
    //пустота
    val empty: Boolean = false,
    val refreshing: Boolean = false,
)
