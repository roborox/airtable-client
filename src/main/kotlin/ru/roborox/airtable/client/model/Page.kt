package ru.roborox.airtable.client.model

data class Page<T>(
    val records: List<Record<T>>,
    val offset: String?
)
