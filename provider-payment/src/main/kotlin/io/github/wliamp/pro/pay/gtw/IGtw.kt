package io.github.wliamp.pro.pay.gtw

import io.github.wliamp.pro.pay.PaymentRequest
import reactor.core.publisher.Mono

interface IGtw {
    fun authorize(body: PaymentRequest): Mono<Any>
    fun capture(body: PaymentRequest): Mono<Any>
    fun sale(body: PaymentRequest): Mono<Any>
    fun refund(body: PaymentRequest): Mono<Any>
    fun void(body: PaymentRequest): Mono<Any>
}
