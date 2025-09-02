package io.github.wliamp.agr

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class AndBuilder<T> {
    private val criteria = mutableListOf<ICriteria<T>>()

    fun <V> equals(selector: (T) -> V, expected: V): AndBuilder<T> =
        apply { criteria += Criteria.equals(selector, expected) }

    fun <V : Comparable<V>> greaterThan(selector: (T) -> V, threshold: V): AndBuilder<T> =
        apply { criteria += Criteria.greaterThan(selector, threshold) }

    fun <V : Comparable<V>> lessThan(selector: (T) -> V, threshold: V): AndBuilder<T> =
        apply { criteria += Criteria.lessThan(selector, threshold) }

    fun <E> contains(selector: (T) -> Collection<E>, expected: E): AndBuilder<T> =
        apply { criteria += Criteria.contains(selector, expected) }

    fun startsWith(selector: (T) -> String, prefix: String): AndBuilder<T> =
        apply { criteria += Criteria.startsWith(selector, prefix) }

    fun endsWith(selector: (T) -> String, suffix: String): AndBuilder<T> =
        apply { criteria += Criteria.endsWith(selector, suffix) }

    fun custom(predicate: (T) -> Boolean): AndBuilder<T> =
        apply { criteria += Criteria.custom(predicate) }

    fun build(): ICriteria<T> =
        criteria.takeIf { it.isEmpty() }
            ?.let { Criteria.alwaysTrue() }
            ?: criteria.reduce { acc, next -> Criteria.and(acc, next) }

    fun filter(items: List<T>): Mono<List<T>> =
        Flux.fromIterable(items)
            .flatMap { item ->
                build().matches(item)
                    .filter { it }
                    .map { item }
            }
            .collectList()
}

class NotAndBuilder<T> {
    private val criteria = mutableListOf<ICriteria<T>>()

    fun and(vararg criteria: ICriteria<T>): ICriteria<T> =
        criteria.reduce { acc, c -> AndCriteria(acc, c) }

    fun or(vararg criteria: ICriteria<T>): ICriteria<T> =
        criteria.reduce { acc, c -> OrCriteria(acc, c) }

    fun not(block: NotAndBuilder<T>.() -> Unit): NotAndBuilder<T> =
        apply { criteria += Criteria.not(NotAndBuilder<T>().apply(block).build()) }

    fun build(): ICriteria<T> =
        criteria.takeIf { it.isEmpty() }
            ?.let { Criteria.alwaysTrue() }
            ?: criteria.reduce { acc, next -> Criteria.and(acc, next) }

    fun filter(items: List<T>): Mono<List<T>> =
        Flux.fromIterable(items)
            .flatMap { item ->
                build().matches(item)
                    .filter { it }
                    .map { item }
            }
            .collectList()
}

