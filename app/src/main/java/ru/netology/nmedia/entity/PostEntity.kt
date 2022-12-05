package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    @TypeConverters(AttachmentConverter::class)
    val attachment: Attachment?
) {
    fun toDto() = Post(id, author, authorAvatar, content, published, likedByMe, likes, attachment)

    companion object {
        fun fromDto(dto: Post) =
            dto.attachment?.let {
                PostEntity(dto.id, dto.author, dto.authorAvatar, dto.content, dto.published, dto.likedByMe, dto.likes, dto.attachment

                )
            }

    }
    //конвертер для вложений
    class AttachmentConverter {
        @TypeConverter
        fun fromAttachment(attachment: Attachment?): String{
            if (attachment != null) {
                return attachment.url
            } else return ""

        }

        @TypeConverter
        fun toAttachment(value: String): Attachment{
            return Attachment(value, "", "")
        }

    }
}



