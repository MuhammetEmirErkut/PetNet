package com.muham.petv01.Inheritance

data class ItemForPost(
    val userPhoto: String,
    val userName: String,
    val time: String,
    val title: String,
    val content: String,
    val documentId: String,
    var like: Int,
    var likedByCurrentUser: Boolean,
    var comments: List<Comment> = mutableListOf()
) {
    // Boş argüman yapıcısı
    constructor() : this("", "", "", "", "", "", 0, false, mutableListOf())
}

data class Comment(
    val userId: String,
    val userName: String,
    val content: String
) {
    // Boş argüman yapıcısı
    constructor() : this("", "", "")
}

