package ru.roborox.airtable

import reactor.core.publisher.Mono
import ru.roborox.airtable.client.AirtableClient
import ru.roborox.airtable.client.model.Page
import ru.roborox.airtable.client.model.PatchingRecord

class AirTableImpl<T>(
    private val tableName: String,
    private val type: Class<T>,
    private val client: AirtableClient
) : AirTable<T> {

    override fun getRecords(pageSize: Int?, offset: String?): Mono<Page<T>> {
        return client.getRecords(
            tableName = tableName,
            type = type,
            pageSize = pageSize,
            offset = offset
        )
    }

    override fun createRecords(fields: List<T>): Mono<Page<T>> {
        return client.createRecords(
            tableName = tableName,
            type = type,
            fields = fields
        )
    }

    override fun patchRecords(records: List<PatchingRecord<T>>): Mono<Page<T>> {
        return client.patchRecords(
            tableName = tableName,
            type = type,
            records = records
        )
    }
}