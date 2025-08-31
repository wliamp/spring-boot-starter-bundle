package io.github.wliamp.pro.pay.impl

import io.github.wliamp.pro.pay.cus.OCus
import reactor.core.publisher.Mono

interface IGtw<T : OCus> {
    fun authorize(request: T): Mono<Any>
    fun capture(request: T): Mono<Any>
    fun sale(request: T): Mono<Any>
    fun refund(request: T): Mono<Any>
    fun void(request: T): Mono<Any>
}
