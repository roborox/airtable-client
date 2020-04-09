package ru.roborox.airtableclient

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.http.MediaType
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import kotlin.properties.Delegates

class AirtableClient {
    var client: WebClient by Delegates.notNull()
    var token: String by Delegates.notNull()

    init {
        val objectMapper = ObjectMapper()
            .registerModule(KotlinModule())
            .registerModule(Jdk8Module())
            .registerModule(JavaTimeModule())

        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
        val strategies = ExchangeStrategies
            .builder()
            .codecs {
                it.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON))
                it.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON))
            }.build()


        /*val prop = Properties()
        prop.load(FileInputStream(this::class.java.classLoader.getResource("airtable.properties").file))*/

        //token = prop.getProperty("airTableToken")
        token = "keyaxOQ0aRPZdX7LA"
        client = WebClient.builder().exchangeStrategies(strategies).baseUrl("https://api.airtable.com/v0/").build()
        //client = WebClient.builder().exchangeStrategies(strategies).baseUrl(prop.getProperty("airTableUrl")).build()
    }

    inline fun <reified T> getRecords(url: String): Mono<Page<T>> {
        return client.get()
            .uri(url)
            .header("Authorization", "Bearer $token")
            .retrieve()
            .bodyToMono()
    }
}