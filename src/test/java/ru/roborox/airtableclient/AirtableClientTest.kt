package ru.roborox.airtableclient

import org.springframework.web.reactive.function.client.WebClientResponseException
import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import ru.roborox.airtableclient.dto.BlackList

@Test
class AirtableClientTest {
    fun getRecords() {
        val client = AirtableClient<BlackList>( "https://api.airtable.com/v0/")
        val records = client.getRecords("appDRAaP5cPajl3Xh/blacklist?pageSize=3", "Bearer keyaxOQ0aRPZdX7LA").block()!!
        assertEquals(records.offset.split("/")[1], "recAEPvdDtk3aXP3H")
    }

    @Test(expectedExceptions = [WebClientResponseException.NotFound::class])
    fun notFound() {
        val client = AirtableClient<BlackList>("https://api.airtable.com/v0")
        client.getRecords("appDRAaP5cPajl3fh/blacklist?pageSize=3", "Bearer keyaxOQ0aRPZdX7LA").block()!!
    }
}