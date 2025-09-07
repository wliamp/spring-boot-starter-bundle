package io.github.wliamp.agr

import reactor.core.publisher.Mono

interface ICriteria<T> {
    fun matches(target: T): Mono<Boolean>
}
