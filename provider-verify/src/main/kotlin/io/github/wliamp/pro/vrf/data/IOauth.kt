package io.github.wliamp.pro.vrf.data

import reactor.core.publisher.Mono

interface IOauth {
    fun verify(token: String): Mono<Boolean>
    fun getInfo(token: String): Mono<Map<String, Any>>
}
