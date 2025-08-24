package io.github.wliamp.provider.data

import reactor.core.publisher.Mono

interface Oauth {
    fun verify(token: String): Mono<Boolean>
    fun getInfo(token: String): Mono<Map<String, Any>>
}
