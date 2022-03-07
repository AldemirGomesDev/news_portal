package com.aldemir.newsportal.api.models

data class ResponseNew (
    var pagination: Pagination,
    var data: List<Data>
) {
    data class Pagination (
        var current_page: Int,
        var per_page: Int,
        var total_pages: Int,
        var total_items: Int
    )

    data class Data(
        var title: String,
        var description: String,
        var content: String,
        var author: String,
        var published_at: String,
        var highlight: Boolean,
        var url: String,
        var image_url: String
    )
}
