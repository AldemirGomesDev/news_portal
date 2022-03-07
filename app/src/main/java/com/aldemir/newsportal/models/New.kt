package com.aldemir.newsportal.models

import androidx.room.*
import java.util.Date

@Entity(tableName = "New", indices = [Index(value = ["title"], unique = true) ])
data class New(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0,
    @ColumnInfo(name = "title")
    var title: String = "",
    @ColumnInfo(name = "description")
    var description: String? = "",
    @ColumnInfo(name = "content")
    var content: String? = "",
    @ColumnInfo(name = "author")
    var author: String? = "",
    @ColumnInfo(name = "published_at")
    var published_at: Date? = null,
    @ColumnInfo(name = "highlight")
    var highlight: Boolean? = false,
    @ColumnInfo(name = "url")
    var url: String? = "",
    @ColumnInfo(name = "image_url")
    var image_url: String? = "",
    @ColumnInfo(name = "is_favorite")
    var is_favorite: Boolean = false
)