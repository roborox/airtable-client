package ru.roborox.airtable.client.request

import ru.roborox.airtable.client.model.CreatingRecord

data class CreateRecordsRequest<T>(
    val records: List<CreatingRecord<T>>
)

