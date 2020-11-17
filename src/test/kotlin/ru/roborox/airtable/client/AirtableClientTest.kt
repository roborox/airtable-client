package ru.roborox.airtable.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import ru.roborox.airtable.client.AirtableClient.Companion.getRecords
import ru.roborox.airtable.client.dto.BlackListDto
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
        val page = client.getRecords<BlackListDto>(
            tableName = "blacklist",
            pageSize = 3,
            offset = null
        ).await()

        assertThat(page.offset?.split("/")?.get(1)).isEqualTo("recAEPvdDtk3aXP3H")
        assertThat(page.records[0].fields.item).isEqualTo("0x5785b3c9b2e62665a8cba0f7bd50dc70ddbd0859")
    }

    @Test
    fun getRecordsWithoutOffset() {
        val page = client.getRecords<BlackListDto>(
            tableName = "blacklist",
            pageSize = null,
            offset = null
        ).await()

        assertThat(page.offset).isNull()
        assertThat(page.records[0].fields.item).isEqualTo("0x5785b3c9b2e62665a8cba0f7bd50dc70ddbd0859")
    }

    @Test
    fun notFound() {
        assertThrows<WebClientResponseException.NotFound> {
            client.getRecords<BlackListDto>(
                tableName = "_blacklist",
                pageSize = null,
                offset = null
            ).block()
        }
    }

    private fun <T: Any> Mono<T>.await(): T = block() ?: error("Null client response")

    private fun getProperty(name: String): String {
        return properties.getProperty(name) ?: System.getenv(name) ?: error("Can't get property $name")
    }
}
