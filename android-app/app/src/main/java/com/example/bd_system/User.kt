package com.example.bd_system

data class User(
    val id: Int,
    val fullName: String,
    val email: String,
    val bloodGroup: String,
    val phone: String,
    val address: String,
    val role: String?
)
