package com.muham.petv01.Accounts

class Persons(
    private val uid: String,
    private val firstName: String,
    private val lastName: String,
    private val password: String,
    private val email: String,
    val pets: List<Pets> = mutableListOf(),
    private val userPhoto: String
) {

    fun getUid(): String {
        return uid
    }

    fun getFirstName(): String {
        return firstName
    }

    fun getLastName(): String {
        return lastName
    }

    fun getPassword(): String {
        return password
    }

    fun getEmail(): String {
        return email
    }

    constructor() : this("", "", "", "", "", mutableListOf(), "")
}
data class Pets(
    val age: String,
    val name: String,
    val type: String,
    val id: String
) {
    // Boş argüman yapıcısı
    constructor() : this("", "", "","")
}

