package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "persons")
data class Person(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val teluguName: String = "",
    val phoneNumber: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
