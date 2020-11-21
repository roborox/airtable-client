package ru.roborox.airtable.client.request

import ru.roborox.airtable.client.model.PatchingRecord

data class PatchRecordsRequest<T>(
    val records: List<PatchingRecord<T>>
)