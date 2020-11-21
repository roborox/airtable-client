package ru.roborox.airtable.client.model

data class PatchingRecord<T>(
    val id: String,
    val fields: T
)