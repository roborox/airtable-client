package ru.roborox.airtable.client.model

import java.time.LocalDateTime

data class Record<T>(
    val id: String,
    val createdTime: LocalDateTime,
    val fields: T
)
