package io.github.wliamp.agr.exe

import io.github.wliamp.agr.impl.CriteriaMatcher
import io.github.wliamp.agr.impl.ICriteria
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentLinkedQueue

class Queue<T> {
    private val queue = ConcurrentLinkedQueue<T & Any>()

    fun enqueue(item: T) = queue.add(item)

    fun dequeue(): T? = queue.poll()

    fun peek(): T? = queue.peek()

    fun filter(criteria: List<ICriteria<T>>): Mono<List<T>> =
        CriteriaMatcher.filter(queue.toList(), criteria)

    fun filter(criteria: ICriteria<T>): Mono<List<T>> =
        filter(listOf(criteria))

    fun dequeueBy(criteria: ICriteria<T>): Mono<T?> =
        filter(criteria)
            .map { it.firstOrNull() }
            .doOnNext { match -> if (match != null) queue.remove(match) }
}
