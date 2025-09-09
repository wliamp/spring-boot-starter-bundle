package io.github.wliamp.pro.vrf

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.*
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier

class GoogleTest {

    private lateinit var server: MockWebServer
    private lateinit var client: WebClient
    private lateinit var props: Properties.GoogleProps
    private lateinit var google: IGoogle
    private val mapper = ObjectMapper()

    @BeforeEach
    fun setup() {
        server = MockWebServer()
        server.start()

        props = Properties.GoogleProps().apply {
            clientId = "test-client"
            baseUrl = server.url("/tokeninfo").toString()
        }

        client = WebClient.builder().build()
        google = IGoogle(props, client)
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `verify returns true when aud matches`() {
        val responseBody = mapOf("aud" to "test-client")
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mapper.writeValueAsString(responseBody))
        )

        StepVerifier.create(google.verify("dummy-token"))
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `verify returns false when aud mismatches`() {
        val responseBody = mapOf("aud" to "other-client")
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mapper.writeValueAsString(responseBody))
        )

        StepVerifier.create(google.verify("dummy-token"))
            .expectNext(false)
            .verifyComplete()
    }

    @Test
    fun `verify throws when aud missing`() {
        val responseBody = mapOf("sub" to "123456")
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mapper.writeValueAsString(responseBody))
        )

        StepVerifier.create(google.verify("dummy-token"))
            .expectError(GoogleParseException::class.java)
            .verify()
    }

    @Test
    fun `getInfo returns payload`() {
        val responseBody = mapOf("aud" to "test-client", "email" to "user@example.com")
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mapper.writeValueAsString(responseBody))
        )

        StepVerifier.create(google.getInfo("dummy-token"))
            .expectNextMatches { it["email"] == "user@example.com" }
            .verifyComplete()
    }

    @Test
    fun `verify errors when config missing clientId`() {
        val propsNoClient = Properties.GoogleProps().apply {
            clientId = ""
            baseUrl = server.url("/tokeninfo").toString()
        }
        val googleNoClient = IGoogle(propsNoClient, client)

        StepVerifier.create(googleNoClient.verify("dummy-token"))
            .expectError(GoogleConfigException::class.java)
            .verify()
    }

    @Test
    fun `verify errors on HTTP error`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody("Bad Request")
        )

        StepVerifier.create(google.verify("dummy-token"))
            .expectError(GoogleHttpException::class.java)
            .verify()
    }
}
