package io.github.wliamp.pro.vrf

import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

internal class OtpFirebase internal constructor(
    private val props: OtpProps.FirebaseProps,
    private val webClient: WebClient
) : IOtp {
    private val otp = Otp.FIREBASE.name

    override fun verify(code: String): Mono<Boolean> =
        fetchFirebase(code)
            .map {
                it["phoneNumber"]?.toString()
                    ?: throw VerifyParseException(otp, "Missing 'phoneNumber' in response")
                true
            }

    override fun getInfo(code: String): Mono<Map<String, Any>> =
        fetchFirebase(code)

    private fun fetchFirebase(code: String): Mono<Map<String, Any>> =
        props.takeIf { it.apiKey.isNotBlank() }
            ?.let {
                webClient.fetchPayload(
                    HttpMethod.POST,
                    "${props.baseUrl}${props.version}${props.uri}",
                    otp,
                    queryParams = mapOf("key" to props.apiKey),
                    body = mapOf(
                        "sessionInfo" to code,
                        "code" to code
                    )
                )
            } ?: Mono.error(
            VerifyConfigException(
                otp,
                "Missing " +
                    "'provider.otp.firebase.api-key'"
            )
        )
}

