package ru.roborox.airtableclient

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.web.reactive.function.client.WebClientResponseException
import ru.roborox.airtableclient.dto.BlackListDTO

@Tag("integration")
class AirtableClientTest {
    private val baseUrl = System.getenv("DEV_AIRTABLE_BASE_URL")
    private val token = System.getenv("DEV_AIRTABLE_TOKEN")

    @Test
    fun getRecords() {
        val client = AirtableClient(baseUrl, token, BlackListDTO::class.java)
        val url = "blacklist?pageSize=3"
        val page = client.getRecords(url).block()!!
        assertThat(page.offset?.split("/")?.get(1)).isEqualTo("recAEPvdDtk3aXP3H")
        assertThat(page.records[0].fields.item).isEqualTo("0x5785b3c9b2e62665a8cba0f7bd50dc70ddbd0859")
    }

    @Test
    fun getRecordsWithoutOffset() {
        val client = AirtableClient(baseUrl, token, BlackListDTO::class.java)
        val url = "blacklist"
        val page = client.getRecords(url).block()!!

        assertThat(page.offset).isNull()
        assertThat(page.records[0].fields.item).isEqualTo("0x5785b3c9b2e62665a8cba0f7bd50dc70ddbd0859")
    }

    @Test
    fun notFound() {
        assertThrows<WebClientResponseException.NotFound> {
            val client = AirtableClient(baseUrl, token, BlackListDTO::class.java)
            val incorrectUrl = "_blacklist?pageSize=3"
            client.getRecords(incorrectUrl).block()!!
        }
    }
}