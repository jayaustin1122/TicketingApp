package com.example.smartticketing.model

data class RegisteredDrivers(
    val address: String = "",
    val birthdate: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val licensedNumber: String = "",
    val sex: String = "",
    val timestamp: String = "",
    val image: String = "",
    val plate: String = ""
)
