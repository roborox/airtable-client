package ru.roborox.airtable.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import ru.roborox.airtable.client.AirtableClient.Companion.createRecords
import ru.roborox.airtable.client.AirtableClient.Companion.getRecords
import ru.roborox.airtable.client.AirtableClient.Companion.patchRecords
import ru.roborox.airtable.client.dto.DesignProjectDto
import ru.roborox.airtable.client.model.PatchingRecord
import java.util.*

@Tag("integration")
class AirtableClientTest {
    private val properties = Properties().also {
        it.load(this::class.java.classLoader.getResourceAsStream("local.properties"))
    }
    private val baseId = getProperty("DEV_AIRTABLE_BASE_ID")
    private val apiKey = getProperty("DEV_AIRTABLE_API_KEY")

    private val client = AirtableClient(baseId = baseId, apiKey = apiKey)

    @Test
    fun getRecords() {
        val page = client.getRecords<DesignProjectDto>(
            tableName = "Design projects",
            pageSize = 3,
            offset = null
        ).await()

        assertThat(page.records).hasSize(3)
    }

    @Test
    fun createRecords() {
        val dp1 = DesignProjectDto(
            kickoffDate = "2020-09-10",
            category = "Technology design",
            name = "Test name 1"
        )
        val dp2 = DesignProjectDto(
            kickoffDate = "2020-09-11",
            category = "Brand identity",
            name = "Test name 2"
        )
        val page = client.createRecords(
            tableName = "Design projects",
            fields = listOf(dp1, dp2)
        ).await()

        assertThat(page.records).hasSize(2)
        assertThat(page.records[0].fields).isEqualTo(dp1)
        assertThat(page.records[1].fields).isEqualTo(dp2)
    }

    @Test
    fun patchRecords() {
        val dp1 = DesignProjectDto(
            kickoffDate = "2020-09-10",
            category = "Technology design",
            name = "Test name 1"
        )
        val dp2 = DesignProjectDto(
            kickoffDate = "2020-09-11",
            category = "Brand identity",
            name = "Test name 2"
        )
        val page = client.createRecords(
            tableName = "Design projects",
            fields = listOf(dp1, dp2)
        ).await()

        val dp1Id = page.records[0].id
        val dp2Id = page.records[1].id

        val patchingRecords = listOf(
            PatchingRecord(
                id = dp1Id,
                fields = dp1.copy(name = "New name 1")
            ),
            PatchingRecord(
                id = dp2Id,
                fields = dp2.copy(name = "New name 2")
            )
        )
        val patchedPage = client.patchRecords(
            tableName = "Design projects",
            records = patchingRecords
        ).await()

        assertThat(patchedPage.records).hasSize(2)
        assertThat(patchedPage.records[0].fields.name).isEqualTo("New name 1")
        assertThat(patchedPage.records[1].fields.name).isEqualTo("New name 2")
    }

    private fun <T: Any> Mono<T>.await(): T = block() ?: error("Null client response")

    private fun getProperty(name: String): String {
        return properties.getProperty(name) ?: System.getenv(name) ?: error("Can't get property $name")
    }
}
