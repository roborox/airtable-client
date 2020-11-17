package ru.roborox.airtable.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.ResolvableType
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.DefaultUriBuilderFactory
import org.springframework.web.util.UriBuilderFactory
import reactor.core.publisher.Mono
import ru.roborox.airtable.client.model.Page
import ru.roborox.airtable.client.request.CreateRecordsRequest
import ru.roborox.airtable.client.request.PatchRecordsRequest
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class AirtableClient(
    baseId: String,
    apiKey: String
) {
    private val uriBuilderFactory: UriBuilderFactory = DefaultUriBuilderFactory(AIRTABLE_API_URL+baseId)

    private val client = WebClient.builder().apply {
        val objectMapper = ObjectMapper()
            .registerModule(KotlinModule())
            .registerModule(Jdk8Module())
            .registerModule(JavaTimeModule())

        val strategies = ExchangeStrategies
            .builder()
            .codecs { configurer ->
                configurer.defaultCodecs().jackson2JsonEncoder(
                    Jackson2JsonEncoder(
                        objectMapper,
                        MediaType.APPLICATION_JSON
                    )
                )
                configurer.defaultCodecs().jackson2JsonDecoder(
                    Jackson2JsonDecoder(
                        objectMapper,
                        MediaType.APPLICATION_JSON
                    )
                )
            }.build()

        it.exchangeStrategies(strategies)
    }.build()

    fun <T> getRecords(
        tableName: String,
        type: Class<T>,
        pageSize: Int?,
        offset: String?
    ): Mono<Page<T>> {
        val pageType = createPageType(type)

        val url = uriBuilderFactory.builder().run {
            pathSegment(tableName.decode())
            pageSize?.let { queryParam("pageSize", it) }
            offset?.let { queryParam("offset", it) }
            build()
        }
        return client.get()
            .uri(url)
            .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
            .retrieve()
            .bodyToMono(pageType)
    }

    fun <T> patchRecords(
        tableName: String,
        request: PatchRecordsRequest<T>,
        type: Class<T>
    ): Mono<Page<T>> {
        val pageType = createPageType(type)

        val url = uriBuilderFactory.builder().run {
            pathSegment(tableName.decode())
            build()
        }
        return client.patch()
            .uri(url)
            .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(pageType)
    }

    fun <T> createRecords(
        tableName: String,
        request: CreateRecordsRequest<T>,
        type: Class<T>
    ): Mono<Page<T>> {
        val pageType = createPageType(type)

        val url = uriBuilderFactory.builder().run {
            pathSegment(tableName.decode())
            build()
        }
        return client.post()
            .uri(url)
            .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(pageType)
    }

    private fun <T> createPageType(type: Class<T>) =
        ParameterizedTypeReference.forType<Page<T>>(
            ResolvableType.forClassWithGenerics(Page::class.java, type).type
        )

    private fun String.decode(): String = URLDecoder.decode(this, StandardCharsets.UTF_8)

    private val authorizationHeader: String = "Bearer $apiKey"

    companion object {
        private const val AIRTABLE_API_URL: String = "https://api.airtable.com/v0/"

        inline fun <reified T> AirtableClient.getRecords(
            tableName: String,
            pageSize: Int? = null,
            offset: String? = null
        ): Mono<Page<T>> {
            return getRecords(tableName, T::class.java, pageSize = pageSize, offset = offset)
        }

        inline fun <reified T> AirtableClient.patchRecords(
            tableName: String,
            request: PatchRecordsRequest<T>
        ): Mono<Page<T>> {
            return patchRecords(tableName, request, T::class.java)
        }

        inline fun <reified T> AirtableClient.createRecords(
            tableName: String,
            request: CreateRecordsRequest<T>
        ): Mono<Page<T>> {
            return createRecords(tableName, request, T::class.java)
        }
    }
}

