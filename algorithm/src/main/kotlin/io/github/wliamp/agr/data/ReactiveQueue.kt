package io.github.wliamp.agr.data

import io.github.wliamp.agr.impl.CriteriaMatcher
import io.github.wliamp.agr.impl.ICriteria
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentLinkedQueue

class ReactiveQueue<T : Any> {
    private val queue = ConcurrentLinkedQueue<T>()

    /** ---------- Basic Actions ---------- **/

    fun enqueue(item: T): Mono<Boolean> =
        Mono.fromCallable { queue.add(item) }

    fun enqueueAll(items: Collection<T>): Mono<Boolean> =
        Mono.fromCallable { queue.addAll(items) }

    fun dequeue(): Mono<T> =
        Mono.fromCallable { queue.poll() }
            .flatMap { Mono.justOrEmpty(it) }

    fun peek(): Mono<T> =
        Mono.fromCallable { queue.peek() }
            .flatMap { Mono.justOrEmpty(it) }

    /** ---------- Query Actions ---------- **/

    fun size(): Mono<Int> =
        Mono.fromCallable { queue.size }

    fun isEmpty(): Mono<Boolean> =
        Mono.fromCallable { queue.isEmpty() }

    fun contains(item: T): Mono<Boolean> =
        Mono.fromCallable { queue.contains(item) }

    fun toList(): Mono<List<T>> =
        Mono.fromCallable { queue.toList() }

    /** ---------- Criteria-based Actions ---------- **/

    fun filter(criteria: List<ICriteria<T>>): Mono<List<T>> =
        CriteriaMatcher.filter(queue.toList(), criteria)

    fun filter(criteria: ICriteria<T>): Mono<List<T>> =
        filter(listOf(criteria))

    fun dequeueBy(criteria: ICriteria<T>): Mono<T> =
        filter(criteria)
            .map { it.firstOrNull() }
            .flatMap { match ->
                if (match != null && queue.remove(match)) Mono.just(match)
                else Mono.empty()
            }

    fun removeBy(criteria: ICriteria<T>): Mono<Boolean> =
        filter(criteria)
            .map { queue.removeAll(it) }

    /** ---------- Maintenance Actions ---------- **/

    fun clear(): Mono<Void> =
        Mono.fromRunnable { queue.clear() }
}

