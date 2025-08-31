package io.github.wliamp.pro.pay.impl

import io.github.wliamp.pro.pay.cus.OCus
import io.github.wliamp.pro.pay.sys.OSys
import reactor.core.publisher.Mono

interface IGtw<C : OCus, S : OSys> {
    fun authorize(cus: C, sys: S): Mono<Any>
    fun capture(cus: C, sys: S): Mono<Any>
    fun sale(cus: C, sys: S): Mono<Any>
    fun refund(cus: C, sys: S): Mono<Any>
    fun void(cus: C, sys: S): Mono<Any>
}
