package io.github.wliamp.agr.impl

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

object Criteria {
    fun <T, V> equals(selector: (T) -> V, expected: V): ICriteria<T> =
        EqualsCriteria(selector, expected)

    fun <T : Comparable<T>> range(selector: (T) -> T, min: T, max: T): ICriteria<T> =
        RangeCriteria(selector, min, max)

    fun <T : Comparable<T>> greaterThan(selector: (T) -> T, threshold: T): ICriteria<T> =
        GreaterThanCriteria(selector, threshold)

    fun <T : Comparable<T>> lessThan(selector: (T) -> T, threshold: T): ICriteria<T> =
        LessThanCriteria(selector, threshold)

    fun <T, E> contains(selector: (T) -> Collection<E>, expected: E): ICriteria<T> =
        ContainsCriteria(selector, expected)

    fun <T> startsWith(selector: (T) -> String, prefix: String): ICriteria<T> =
        StartsWithCriteria(selector, prefix)

    fun <T> endsWith(selector: (T) -> String, suffix: String): ICriteria<T> =
        EndsWithCriteria(selector, suffix)

    fun <T> custom(predicate: (T) -> Boolean): ICriteria<T> =
        CustomCriteria(predicate)

    fun <T> and(left: ICriteria<T>, right: ICriteria<T>): ICriteria<T> =
        AndCriteria(left, right)

    fun <T> or(left: ICriteria<T>, right: ICriteria<T>): ICriteria<T> =
        OrCriteria(left, right)

    fun <T> not(inner: ICriteria<T>): ICriteria<T> =
        NotCriteria(inner)

    fun <T> alwaysTrue(): ICriteria<T> =
        object : ICriteria<T> {
            override fun matches(target: T): Mono<Boolean> = Mono.just(true)
        }

    fun <T> alwaysFalse(): ICriteria<T> =
        object : ICriteria<T> {
            override fun matches(target: T): Mono<Boolean> = Mono.just(false)
        }
}

object CriteriaMatcher {
    fun <T> filter(
        items: List<T>,
        criteria: List<ICriteria<T>>
    ): Mono<List<T>> =
        Flux.fromIterable(items)
            .flatMap { item ->
                Flux.fromIterable(criteria)
                    .flatMap { it.matches(item) }
                    .all { it }
                    .filter { it }
                    .map { item }
            }
            .collectList()
}
