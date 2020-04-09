package ru.roborox.airtableclient

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import ru.roborox.airtableclient.dto.common.Page
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.http.codec.json.Jackson2JsonEncoder
import kotlin.properties.Delegates


class AirtableClient<T>(baseUrl: String) {
    var client : WebClient by Delegates.notNull()

    init {
        val objectMapper = ObjectMapper()
            .registerModule(KotlinModule())
            .registerModule(Jdk8Module())
            .registerModule(JavaTimeModule())

        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
        val strategies = ExchangeStrategies
            .builder()
            .codecs { clientDefaultCodecsConfigurer ->
                clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON))
                clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON))
            }.build()
        client = WebClient.builder().exchangeStrategies(strategies).baseUrl(baseUrl).build()
    }

    fun getRecords(url: String, token: String): Mono<Page<T>> {
        return client.get()
            .uri(url)
            .header("Authorization", token)
            .retrieve()
            .bodyToMono()
    }
}