package io.github.wliamp.agr.impl

import reactor.core.publisher.Mono

interface ICriteria {
    fun matches(other: ICriteria): Mono<Boolean>
}
