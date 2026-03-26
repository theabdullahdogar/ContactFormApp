package com.example.contactformapp

import java.io.Serializable

data class Contact(
    val id: Long = System.currentTimeMillis(),
    var name: String,
    var phone: String,
    var email: String = "",
    var address: String = "",
    var profileImageUri: String = ""
) : Serializable