package io.wliamp.token.data

import reactor.core.publisher.Mono

interface OauthParty {
    fun verify(token: String): Mono<Boolean>
    fun getInfo(token: String): Mono<Map<String, Any>>
}
