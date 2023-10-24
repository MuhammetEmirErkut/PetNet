package com.muham.petv01.Accounts

class Persons(
    private val uid: String,
    private val firstName: String,
    private val lastName: String,
    private val password: String,
    private val email: String
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
}