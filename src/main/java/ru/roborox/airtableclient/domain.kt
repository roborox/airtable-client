package ru.roborox.airtableclient

import java.time.LocalDateTime

@Deprecated("Should use ru.roborox.airtable.client.model.Page")
data class Page<T>(
    val records: List<Record<T>>,
    val offset: String? = null
)

@Deprecated("Should use ru.roborox.airtable.client.model.Record")
data class Record<T>(
    val id: String,
    val fields: T,
    val createdTime: LocalDateTime
)