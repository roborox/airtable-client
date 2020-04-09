package ru.roborox.airtableclient.dto.common

data class Page<T>(val records: List<Record<T>>, val offset: String)