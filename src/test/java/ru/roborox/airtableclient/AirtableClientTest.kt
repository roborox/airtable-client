package ru.roborox.airtableclient

import org.springframework.web.reactive.function.client.WebClientResponseException
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test
class AirtableClientTest {
    private val baseUrl = "https://api.airtable.com/v0/"
    private val token = "keyaxOQ0aRPZdX7LA"

    fun getRecords() {
        val client = AirtableClient(baseUrl, BlackListDTO::class.java)
        val url = "appDRAaP5cPajl3Xh/blacklist?pageSize=3"
        val page = client.getRecords(url, token).block()!!
        assertEquals(page.offset.split("/")[1], "recAEPvdDtk3aXP3H")
        assertEquals(page.records[0].fields.item, "0x5785b3c9b2e62665a8cba0f7bd50dc70ddbd0859")
    }

    fun getRecordsWithoutOffset() {
        val client = AirtableClient(baseUrl, BlackListDTO::class.java)
        val url = "appDRAaP5cPajl3Xh/blacklist"
        val page = client.getRecords(url, token).block()!!
        assertEquals(page.offset, "")
        assertEquals(page.records[0].fields.item, "0x5785b3c9b2e62665a8cba0f7bd50dc70ddbd0859")
    }

    @Test(expectedExceptions = [WebClientResponseException.NotFound::class])
    fun notFound() {
        val client = AirtableClient(baseUrl, BlackListDTO::class.java)
        val incorrectUrl = "appDRAaP5cPajl3fh/blacklist?pageSize=3"
        client.getRecords(incorrectUrl, token).block()!!
    }
}