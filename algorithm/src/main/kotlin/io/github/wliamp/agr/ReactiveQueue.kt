package io.github.wliamp.agr

import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentLinkedQueue

class ReactiveQueue<T : Any> {
    private val queue = ConcurrentLinkedQueue<T>()

    /** ---------- Basic Actions ---------- **/

    /**
     * Adds a single item to the queue.
     * @return Mono<Boolean> indicating whether the item was added successfully.
     */
    fun enqueue(item: T): Mono<Boolean> =
        Mono.fromCallable { queue.add(item) }

    /**
     * Adds multiple items to the queue at once.
     * @return Mono<Boolean> indicating whether all items were added successfully.
     */
    fun enqueueAll(items: Collection<T>): Mono<Boolean> =
        Mono.fromCallable { queue.addAll(items) }

    /**
     * Removes and returns the head of the queue.
     * @return Mono<T> containing the removed item, or empty if the queue is empty.
     */
    fun dequeue(): Mono<T> =
        Mono.fromCallable { queue.poll() }
            .flatMap { Mono.justOrEmpty(it) }

    /**
     * Returns the head of the queue without removing it.
     * @return Mono<T> containing the head item, or empty if the queue is empty.
     */
    fun peek(): Mono<T> =
        Mono.fromCallable { queue.peek() }
            .flatMap { Mono.justOrEmpty(it) }

    /** ---------- Query Actions ---------- **/

    /**
     * Returns the current size of the queue.
     * @return Mono<Int> representing the number of items in the queue.
     */
    fun size(): Mono<Int> =
        Mono.fromCallable { queue.size }

    /**
     * Checks if the queue is empty.
     * @return Mono<Boolean> indicating whether the queue contains no items.
     */
    fun isEmpty(): Mono<Boolean> =
        Mono.fromCallable { queue.isEmpty() }

    /**
     * Checks if the queue contains the given item.
     * @param item the item to check for.
     * @return Mono<Boolean> indicating presence of the item.
     */
    fun contains(item: T): Mono<Boolean> =
        Mono.fromCallable { queue.contains(item) }

    /**
     * Returns a snapshot of all items in the queue as a List.
     * @return Mono<List<T>> representing all items currently in the queue.
     */
    fun toList(): Mono<List<T>> =
        Mono.fromCallable { queue.toList() }

    /** ---------- Criteria-based Actions ---------- **/

    /**
     * Filters items in the queue based on a list of criteria.
     * @param criteria List of ICriteria<T> to apply.
     * @return Mono<List<T>> containing items that match all criteria.
     */
    fun filter(criteria: List<ICriteria<T>>): Mono<List<T>> =
        Criteria.filter(queue.toList(), criteria)

    /**
     * Filters items in the queue based on a single criterion.
     * @param criteria ICriteria<T> to apply.
     * @return Mono<List<T>> containing items that match the criterion.
     */
    fun filter(criteria: ICriteria<T>): Mono<List<T>> =
        filter(listOf(criteria))

    /**
     * Removes and returns the first item that matches the given criterion.
     * @param criteria ICriteria<T> to match.
     * @return Mono<T> containing the removed item, or empty if none matched.
     */
    fun dequeueBy(criteria: ICriteria<T>): Mono<T> =
        filter(criteria)
            .map { it.firstOrNull() }
            .flatMap { match ->
                if (match != null && queue.remove(match)) Mono.just(match)
                else Mono.empty()
            }

    /**
     * Removes all items that match the given criterion from the queue.
     * @param criteria ICriteria<T> to match.
     * @return Mono<Boolean> indicating whether any items were removed.
     */
    fun removeBy(criteria: ICriteria<T>): Mono<Boolean> =
        filter(criteria)
            .map { queue.removeAll(it) }

    /** ---------- Maintenance Actions ---------- **/

    /**
     * Clears all items from the queue.
     * @return Mono<Void> completing when the queue has been cleared.
     */
    fun clear(): Mono<Void> =
        Mono.fromRunnable { queue.clear() }
}
