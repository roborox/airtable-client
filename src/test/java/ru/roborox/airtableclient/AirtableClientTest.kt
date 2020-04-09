package ru.roborox.airtableclient

import org.springframework.web.reactive.function.client.WebClientResponseException
import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import ru.roborox.airtableclient.dto.BlackListDTO

@Test
class AirtableClientTest {
    fun getRecords() {
        val client = AirtableClient()
        val page = client.getRecords<BlackListDTO>("appDRAaP5cPajl3Xh/blacklist?pageSize=3").block()!!
        assertEquals(page.offset.split("/")[1], "recAEPvdDtk3aXP3H")
        assertEquals(page.records[0].fields.item, "0x5785b3c9b2e62665a8cba0f7bd50dc70ddbd0859")
    }

    @Test(expectedExceptions = [WebClientResponseException.NotFound::class])
    fun notFound() {
        val client = AirtableClient()
        client.getRecords<BlackListDTO>("appDRAaP5cPajl3fh/blacklist?pageSize=3").block()!!
    }
}