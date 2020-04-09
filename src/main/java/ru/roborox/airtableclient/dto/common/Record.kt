package ru.roborox.airtableclient.dto.common

import java.time.LocalDateTime

data class Record<T>(val id: String, val fields: List<T>, val createdTime: LocalDateTime)