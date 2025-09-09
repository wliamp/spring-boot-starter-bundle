package io.github.wliamp.pro.vrf

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier

private class FacebookTest : OauthTest<Properties.FacebookProps>({ _ ->
    Properties.FacebookProps().apply {
        appId = "test-app"
        accessToken = "test-access-token"
        baseUrl = ""
        fields = ""
        vrfUri = "/debug_token"
        infoUri = "/me"
    }
}) {
    override fun buildProvider(props: Properties.FacebookProps, client: WebClient): IOauth =
        IFacebook(props, client)

    @Test
    fun `verify returns true when app_id matches`() {
        enqueueJson(mapOf("data" to mapOf("app_id" to "test-app")))

        StepVerifier.create(provider.verify("dummy-token"))
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `verify returns false when app_id mismatches`() {
        enqueueJson(mapOf("data" to mapOf("app_id" to "other-app")))

        StepVerifier.create(provider.verify("dummy-token"))
            .expectNext(false)
            .verifyComplete()
    }

    @Test
    fun `verify errors when data missing`() {
        enqueueJson(mapOf("no_data" to "oops"))

        StepVerifier.create(provider.verify("dummy-token"))
            .expectError(OauthParseException::class.java)
            .verify()
    }

    @Test
    fun `verify errors when app_id missing`() {
        enqueueJson(mapOf("data" to mapOf("id" to "123456")))

        StepVerifier.create(provider.verify("dummy-token"))
            .expectError(OauthParseException::class.java)
            .verify()
    }

    @Test
    fun `verify errors when config missing appId`() {
        val bad = Properties.FacebookProps().apply {
            appId = ""
            accessToken = "test-access-token"
            baseUrl = ""
        }
        val f = IFacebook(bad, client)

        StepVerifier.create(f.verify("dummy-token"))
            .expectError(OauthConfigException::class.java)
            .verify()
    }

    @Test
    fun `verify errors when config missing accessToken`() {
        val bad = Properties.FacebookProps().apply {
            appId = "test-app"
            accessToken = ""
            baseUrl = ""
        }
        val f = IFacebook(bad, client)

        StepVerifier.create(f.verify("dummy-token"))
            .expectError(OauthConfigException::class.java)
            .verify()
    }

    @Test
    fun `getInfo works when fields config empty`() {
        val bad = Properties.FacebookProps().apply {
            appId = "test-app"
            accessToken = "test-access-token"
            baseUrl = ""
            fields = ""
        }
        val f = IFacebook(bad, client)

        enqueueJson(mapOf("id" to "456"))

        StepVerifier.create(f.getInfo("dummy-token"))
            .expectNextMatches { it["id"] == "456" }
            .verifyComplete()
    }

    @Test
    fun `verify builds correct facebook uri`() {
        enqueueJson(mapOf("data" to mapOf("app_id" to "test-app")))

        StepVerifier.create(provider.verify("dummy-token"))
            .expectNext(true)
            .verifyComplete()

        val recorded = server.takeRequest()
        assertEquals(
            "${props.vrfUri}?input_token=dummy-token&access_token=test-access-token",
            recorded.path
        )
    }

    @Test
    fun `getInfo builds correct uri without fields`() {
        enqueueJson(mapOf("id" to "123", "name" to "Alice"))

        StepVerifier.create(provider.getInfo("dummy-token"))
            .expectNextMatches { it["id"] == "123" && it["name"] == "Alice" }
            .verifyComplete()

        val recorded = server.takeRequest()
        assertEquals(
            "${props.infoUri}?access_token=dummy-token",
            recorded.path
        )
    }

    @Test
    fun `getInfo builds correct uri with fields`() {
        val customProps = Properties.FacebookProps().apply {
            appId = "test-app"
            accessToken = "test-access-token"
            baseUrl = props.baseUrl
            fields = "id,name"
            vrfUri = props.vrfUri
            infoUri = props.infoUri
        }
        val fb = IFacebook(customProps, client)

        enqueueJson(mapOf("id" to "123", "name" to "Alice"))

        StepVerifier.create(fb.getInfo("dummy-token"))
            .expectNextMatches { it["id"] == "123" && it["name"] == "Alice" }
            .verifyComplete()

        val recorded = server.takeRequest()
        assertEquals(
            "${props.infoUri}?access_token=dummy-token&fields=id,name",
            recorded.path
        )
    }
}
