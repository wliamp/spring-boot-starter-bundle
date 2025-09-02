package io.github.wliamp.agr.impl

import reactor.core.publisher.Mono

class EqualsCriteria<T, V>(
    private val selector: (T) -> V,
    private val expected: V
) : ICriteria<T> {
    override fun matches(target: T): Mono<Boolean> =
        Mono.fromSupplier { selector(target) == expected }
}

class RangeCriteria<T : Comparable<T>>(
    private val selector: (T) -> T,
    private val min: T,
    private val max: T
) : ICriteria<T> {
    override fun matches(target: T): Mono<Boolean> =
        Mono.fromSupplier {
            val value = selector(target)
            value in min..max
        }
}

class GreaterThanCriteria<T : Comparable<T>>(
    private val selector: (T) -> T,
    private val threshold: T
) : ICriteria<T> {
    override fun matches(target: T): Mono<Boolean> =
        Mono.fromSupplier { selector(target) > threshold }
}

class LessThanCriteria<T : Comparable<T>>(
    private val selector: (T) -> T,
    private val threshold: T
) : ICriteria<T> {
    override fun matches(target: T): Mono<Boolean> =
        Mono.fromSupplier { selector(target) < threshold }
}

class ContainsCriteria<T, E>(
    private val selector: (T) -> Collection<E>,
    private val expected: E
) : ICriteria<T> {
    override fun matches(target: T): Mono<Boolean> =
        Mono.fromSupplier { selector(target).contains(expected) }
}

class StartsWithCriteria<T>(
    private val selector: (T) -> String,
    private val prefix: String
) : ICriteria<T> {
    override fun matches(target: T): Mono<Boolean> =
        Mono.fromSupplier { selector(target).startsWith(prefix) }
}

class EndsWithCriteria<T>(
    private val selector: (T) -> String,
    private val suffix: String
) : ICriteria<T> {
    override fun matches(target: T): Mono<Boolean> =
        Mono.fromSupplier { selector(target).endsWith(suffix) }
}

class CustomCriteria<T>(
    private val predicate: (T) -> Boolean
) : ICriteria<T> {
    override fun matches(target: T): Mono<Boolean> =
        Mono.fromSupplier { predicate(target) }
}

class AndCriteria<T>(
    private val left: ICriteria<T>,
    private val right: ICriteria<T>
) : ICriteria<T> {
    override fun matches(target: T): Mono<Boolean> =
        left.matches(target)
            .zipWith(right.matches(target)) { l, r -> l && r }
}

class OrCriteria<T>(
    private val left: ICriteria<T>,
    private val right: ICriteria<T>
) : ICriteria<T> {
    override fun matches(target: T): Mono<Boolean> =
        left.matches(target)
            .zipWith(right.matches(target)) { l, r -> l || r }
}

class NotCriteria<T>(
    private val inner: ICriteria<T>
) : ICriteria<T> {
    override fun matches(target: T): Mono<Boolean> =
        inner.matches(target).map { !it }
}
