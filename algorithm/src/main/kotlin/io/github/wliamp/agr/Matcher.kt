package io.github.wliamp.agr

import io.github.wliamp.agr.data.Space
import io.github.wliamp.agr.data.Target
import io.github.wliamp.agr.exe.QueueExecute
import reactor.core.publisher.Mono

class Matcher(private val queue: QueueExecute) {
    fun enqueue(target: Target, spaceTemplate: Space): Mono<Space> =
        queue.addTarget(target)
            .thenMany(queue.matchTargets(spaceTemplate))
            .last()
}
