package com.muham.petv01.Inheritance

data class ItemForPost(
    val userPhoto: String,
    val userName: String,
    val time: String,
    val title: String,
    val content: String,
    val documentId: String,
    var like: Int,
    var likedByCurrentUser: Boolean // Yeni eklenen değişken
)
