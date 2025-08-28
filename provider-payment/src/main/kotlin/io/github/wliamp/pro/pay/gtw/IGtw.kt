package io.github.wliamp.pro.pay.gtw

import reactor.core.publisher.Mono

interface IGtw {
    fun authorize(headers: Any, body: Any): Mono<Any>
    fun capture(headers: Any, body: Any): Mono<Any>
    fun sale(headers: Any, body: Any): Mono<Any>
    fun refund(headers: Any, body: Any): Mono<Any>
    fun void(headers: Any, body: Any): Mono<Any>
}
