import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val client = WebClient.builder().build()
        val result = client.get()
            .uri("https://google.com")
            .retrieve()
            .bodyToMono<String>()
            .block()

        println(result)
    }
}