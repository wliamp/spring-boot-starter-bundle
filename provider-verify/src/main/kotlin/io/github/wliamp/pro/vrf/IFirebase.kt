package io.github.wliamp.pro.vrf

import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

internal class IFirebase internal constructor(
    private val props: OtpProps.FirebaseProps,
    private val webClient: WebClient
) : IOtp {
    private val otp = Otp.FIREBASE

    override fun verify(code: String): Mono<Boolean> =
        props.takeIf { it.apiKey.isNotBlank() }
            ?.let {
                fetchFirebaseOtp(code)
                    .map {
                        it["phoneNumber"] != null
                    }
            } ?: Mono.error(
            RuntimeException("Missing firebaseApiKey in OtpProps")
        )

    override fun getInfo(code: String): Mono<Map<String, Any>> =
        fetchFirebaseOtp(code)

    private fun fetchFirebaseOtp(code: String): Mono<Map<String, Any>> {
        val uri = "${props.baseUrl}${props.version}${props.uri}?key=${props.apiKey}"
        return webClient.post()
            .uri(uri)
            .bodyValue(mapOf("sessionInfo" to code, "code" to code))
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
            .onErrorMap { RuntimeException("Firebase OTP verification failed", it) }
    }
}

