package io.github.wliamp.pro.vrf

import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.test.StepVerifier
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class OauthTest<P : Any>(
    private val propsFactory: (String) -> P
) {
    protected lateinit var server: MockWebServer
    protected lateinit var client: WebClient
    protected lateinit var props: P
    protected lateinit var provider: IOauth
    private val mapper = ObjectMapper()

    abstract fun buildProvider(props: P, client: WebClient): IOauth

    @BeforeAll
    fun beforeAll() { server = MockWebServer(); server.start() }
    @AfterAll
    fun afterAll() { server.shutdown() }

    @BeforeEach
    fun setup() {
        server = MockWebServer()
        server.start()
        val base = server.url("/").toString().removeSuffix("/")

        val httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 200)
            .doOnConnected { c ->
                c.addHandlerLast(ReadTimeoutHandler(1, TimeUnit.SECONDS))
                c.addHandlerLast(WriteTimeoutHandler(1, TimeUnit.SECONDS))
            }

        client = WebClient.builder()
            .baseUrl(base)
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .build()

        props = propsFactory(base)
        provider = buildProvider(props, client)
    }

    @AfterEach
    fun teardown() {
        server.shutdown()
    }

    protected fun enqueueJson(body: Any, code: Int = 200) {
        server.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(mapper.writeValueAsString(body))
        )
    }

    @Test
    fun `http error OauthHttpException`() {
        server.enqueue(MockResponse()
            .setResponseCode(400)
            .setHeader("Content-Type", "application/json")
            .setBody("""{"error":"Bad Request"}"""))

        StepVerifier.create(provider.verify("dummy-token"))
            .expectError(OauthHttpException::class.java)
            .verify()
    }

    @Test
    fun `network error OauthNetworkException`() {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))

        StepVerifier.create(provider.verify("dummy-token"))
            .expectError(OauthNetworkException::class.java)
            .verify()
    }

    @Test
    fun `invalid JSON OauthParseException`() {
        server.enqueue(MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("not-a-json"))

        StepVerifier.create(provider.getInfo("dummy-token"))
            .expectError(OauthParseException::class.java)
            .verify()
    }

    @Test
    fun `getInfo returns payload`() {
        enqueueJson(mapOf("id" to "123", "name" to "Alice"))

        StepVerifier.create(provider.getInfo("dummy-token"))
            .expectNextMatches { it["id"] == "123" }
            .verifyComplete()
    }
}

