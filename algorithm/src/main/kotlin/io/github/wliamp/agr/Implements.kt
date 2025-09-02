package io.github.wliamp.agr

import reactor.core.publisher.Mono

internal class EqualsCriteria<T, V>(
    private val selector: (T) -> V,
    private val expected: V
) : ICriteria<T> {
    override fun matches(target: T): Mono<Boolean> =
        Mono.fromSupplier { selector(target) == expected }
}

internal class RangeCriteria<T : Comparable<T>>(
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

internal class GreaterThanCriteria<T : Comparable<T>>(
    private val selector: (T) -> T,
    private val threshold: T
) : ICriteria<T> {
    override fun matches(target: T): Mono<Boolean> =
        Mono.fromSupplier { selector(target) > threshold }
}

internal class LessThanCriteria<T : Comparable<T>>(
    private val selector: (T) -> T,
    private val threshold: T
) : ICriteria<T> {
    override fun matches(target: T): Mono<Boolean> =
        Mono.fromSupplier { selector(target) < threshold }
}

internal class ContainsCriteria<T, E>(
    private val selector: (T) -> Collection<E>,
    private val expected: E
) : ICriteria<T> {
    override fun matches(target: T): Mono<Boolean> =
        Mono.fromSupplier { selector(target).contains(expected) }
}

internal class StartsWithCriteria<T>(
    private val selector: (T) -> String,
    private val prefix: String
) : ICriteria<T> {
    override fun matches(target: T): Mono<Boolean> =
        Mono.fromSupplier { selector(target).startsWith(prefix) }
}

internal class EndsWithCriteria<T>(
    private val selector: (T) -> String,
    private val suffix: String
) : ICriteria<T> {
    override fun matches(target: T): Mono<Boolean> =
        Mono.fromSupplier { selector(target).endsWith(suffix) }
}

internal class CustomCriteria<T>(
    private val predicate: (T) -> Boolean
) : ICriteria<T> {
    override fun matches(target: T): Mono<Boolean> =
        Mono.fromSupplier { predicate(target) }
}

internal class AndCriteria<T>(
    private val left: ICriteria<T>,
    private val right: ICriteria<T>
) : ICriteria<T> {
    override fun matches(target: T): Mono<Boolean> =
        left.matches(target)
            .zipWith(right.matches(target)) { l, r -> l && r }
}

internal class OrCriteria<T>(
    private val left: ICriteria<T>,
    private val right: ICriteria<T>
) : ICriteria<T> {
    override fun matches(target: T): Mono<Boolean> =
        left.matches(target)
            .zipWith(right.matches(target)) { l, r -> l || r }
}

internal class NotCriteria<T>(
    private val inner: ICriteria<T>
) : ICriteria<T> {
    override fun matches(target: T): Mono<Boolean> =
        inner.matches(target).map { !it }
}
