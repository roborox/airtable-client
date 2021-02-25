package ru.roborox.airtable.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.ResolvableType
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ClientHttpConnector
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.DefaultUriBuilderFactory
import org.springframework.web.util.UriBuilderFactory
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.util.retry.Retry
import ru.roborox.airtable.client.model.CreatingRecord
import ru.roborox.airtable.client.model.Page
import ru.roborox.airtable.client.model.PatchingRecord
import ru.roborox.airtable.client.request.CreateRecordsRequest
import ru.roborox.airtable.client.request.PatchRecordsRequest
import java.net.URLDecoder
import java.time.Duration

class AirtableClient(
    baseId: String,
    apiKey: String
) {
    private val uriBuilderFactory: UriBuilderFactory = DefaultUriBuilderFactory(AIRTABLE_API_URL + baseId)

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

        val httpClient = HttpClient.create()
            .tcpConfiguration { client ->
                client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT * 1000)
                    .doOnConnected { conn ->
                        conn
                            .addHandlerLast(ReadTimeoutHandler(TIMEOUT))
                            .addHandlerLast(WriteTimeoutHandler(TIMEOUT))
                    }
            }

        val connector: ClientHttpConnector = ReactorClientHttpConnector(httpClient)

        it.exchangeStrategies(strategies).clientConnector(connector)
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
            .retryWhen(
                Retry.backoff(MAX_RETRY, Duration.ofMillis(250))
                    .minBackoff(Duration.ofMillis(100))
            )
    }

    fun <T> patchRecords(
        tableName: String,
        records: List<PatchingRecord<T>>,
        type: Class<T>
    ): Mono<Page<T>> {
        val pageType = createPageType(type)

        val url = uriBuilderFactory.builder().run {
            pathSegment(tableName.decode())
            build()
        }
        val request = PatchRecordsRequest(
            records = records
        )
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
        fields: List<T>,
        type: Class<T>
    ): Mono<Page<T>> {
        val pageType = createPageType(type)

        val url = uriBuilderFactory.builder().run {
            pathSegment(tableName.decode())
            build()
        }
        val request = CreateRecordsRequest(
            records = fields.map { CreatingRecord(it) }
        )
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

    private fun String.decode(): String = URLDecoder.decode(this, "UTF-8")

    private val authorizationHeader: String = "Bearer $apiKey"

    companion object {
        private const val AIRTABLE_API_URL: String = "https://api.airtable.com/v0/"
        private const val TIMEOUT = 120
        private const val MAX_RETRY = 5L

        inline fun <reified T> AirtableClient.getRecords(
            tableName: String,
            pageSize: Int? = null,
            offset: String? = null
        ): Mono<Page<T>> {
            return getRecords(tableName, T::class.java, pageSize = pageSize, offset = offset)
        }

        inline fun <reified T> AirtableClient.patchRecords(
            tableName: String,
            records: List<PatchingRecord<T>>
        ): Mono<Page<T>> {
            return patchRecords(tableName, records, T::class.java)
        }

        inline fun <reified T> AirtableClient.createRecords(
            tableName: String,
            fields: List<T>
        ): Mono<Page<T>> {
            return createRecords(tableName, fields, T::class.java)
        }
    }
}

