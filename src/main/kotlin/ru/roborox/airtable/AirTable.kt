package ru.roborox.airtable

import reactor.core.publisher.Mono
import ru.roborox.airtable.client.model.Page
import ru.roborox.airtable.client.model.PatchingRecord

interface AirTable<T> {
    fun getRecords(pageSize: Int?, offset: String?): Mono<Page<T>>

    fun createRecords(fields: List<T>): Mono<Page<T>>

    fun patchRecords(records: List<PatchingRecord<T>>): Mono<Page<T>>
}
