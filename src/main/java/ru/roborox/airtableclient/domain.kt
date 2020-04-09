package ru.roborox.airtableclient

import java.time.LocalDateTime

data class Page<T>(
    val records: List<Record<T>>,
    val offset: String
)

data class Record<T>(
    val id: String,
    val fields: T,
    val createdTime: LocalDateTime
)