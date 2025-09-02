package io.github.wliamp.agr

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

object Criteria {
    /**
     * Creates an equality criterion.
     *
     * @param selector a function to extract the value from the target
     * @param expected the value to compare against
     * @return an ICriteria that matches when the selected value equals the expected value
     */
    fun <T, V> equals(selector: (T) -> V, expected: V): ICriteria<T> =
        EqualsCriteria(selector, expected)

    /**
     * Creates a range criterion.
     *
     * @param selector a function to extract the comparable value from the target
     * @param min the minimum inclusive bound
     * @param max the maximum inclusive bound
     * @return an ICriteria that matches when the selected value is between min and max
     */
    fun <T : Comparable<T>> range(selector: (T) -> T, min: T, max: T): ICriteria<T> =
        RangeCriteria(selector, min, max)

    /**
     * Creates a "greater than" criterion.
     *
     * @param selector a function to extract the comparable value from the target
     * @param threshold the value that the selected value must exceed
     * @return an ICriteria that matches when the selected value is greater than the threshold
     */
    fun <T : Comparable<T>> greaterThan(selector: (T) -> T, threshold: T): ICriteria<T> =
        GreaterThanCriteria(selector, threshold)

    /**
     * Creates a "less than" criterion.
     *
     * @param selector a function to extract the comparable value from the target
     * @param threshold the value that the selected value must be below
     * @return an ICriteria that matches when the selected value is less than the threshold
     */
    fun <T : Comparable<T>> lessThan(selector: (T) -> T, threshold: T): ICriteria<T> =
        LessThanCriteria(selector, threshold)

    /**
     * Creates a "contains" criterion for collections.
     *
     * @param selector a function to extract the collection from the target
     * @param expected the element that must exist in the collection
     * @return an ICriteria that matches when the collection contains the expected element
     */
    fun <T, E> contains(selector: (T) -> Collection<E>, expected: E): ICriteria<T> =
        ContainsCriteria(selector, expected)

    /**
     * Creates a "starts with" criterion for strings.
     *
     * @param selector a function to extract the string from the target
     * @param prefix the prefix that the string must start with
     * @return an ICriteria that matches when the string starts with the prefix
     */
    fun <T> startsWith(selector: (T) -> String, prefix: String): ICriteria<T> =
        StartsWithCriteria(selector, prefix)

    /**
     * Creates an "ends with" criterion for strings.
     *
     * @param selector a function to extract the string from the target
     * @param suffix the suffix that the string must end with
     * @return an ICriteria that matches when the string ends with the suffix
     */
    fun <T> endsWith(selector: (T) -> String, suffix: String): ICriteria<T> =
        EndsWithCriteria(selector, suffix)

    /**
     * Creates a custom criterion using a predicate function.
     *
     * @param predicate a function that returns true if the target matches
     * @return an ICriteria that uses the custom predicate
     */
    fun <T> custom(predicate: (T) -> Boolean): ICriteria<T> =
        CustomCriteria(predicate)

    /**
     * Combines two criteria using logical AND.
     *
     * @param left the left criterion
     * @param right the right criterion
     * @return an ICriteria that matches when both left and right match
     */
    fun <T> and(left: ICriteria<T>, right: ICriteria<T>): ICriteria<T> =
        AndCriteria(left, right)

    /**
     * Combines two criteria using logical OR.
     *
     * @param left the left criterion
     * @param right the right criterion
     * @return an ICriteria that matches when either left or right matches
     */
    fun <T> or(left: ICriteria<T>, right: ICriteria<T>): ICriteria<T> =
        OrCriteria(left, right)

    /**
     * Negates a criterion using logical NOT.
     *
     * @param inner the criterion to negate
     * @return an ICriteria that matches when inner does not match
     */
    fun <T> not(inner: ICriteria<T>): ICriteria<T> =
        NotCriteria(inner)

    /**
     * Returns a criterion that always evaluates to true.
     *
     * @return an ICriteria that always matches
     */
    fun <T> alwaysTrue(): ICriteria<T> =
        object : ICriteria<T> {
            override fun matches(target: T): Mono<Boolean> = Mono.just(true)
        }

    /**
     * Returns a criterion that always evaluates to false.
     *
     * @return an ICriteria that never matches
     */
    fun <T> alwaysFalse(): ICriteria<T> =
        object : ICriteria<T> {
            override fun matches(target: T): Mono<Boolean> = Mono.just(false)
        }

    /**
     * Filters a list of items using a list of criteria.
     * Only items that satisfy all criteria are included in the result.
     *
     * @param items the list of items to filter
     * @param criteria the list of criteria to apply
     * @return a Mono emitting the filtered list
     */
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
