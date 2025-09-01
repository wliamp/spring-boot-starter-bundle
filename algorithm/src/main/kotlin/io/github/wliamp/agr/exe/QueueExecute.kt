package io.github.wliamp.agr.exe

import io.github.wliamp.agr.data.Space
import io.github.wliamp.agr.data.Target
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.concurrent.CopyOnWriteArrayList

class QueueExecute {
    private val enqueueTargets = CopyOnWriteArrayList<Target>()
    private val spaces = CopyOnWriteArrayList<Space>()

    fun addTarget(target: Target): Mono<Target> =
        Mono.fromCallable {
            enqueueTargets += target
            target
        }

    private fun removeTarget(target: Target): Mono<Void> =
        Mono.fromRunnable {
            enqueueTargets -= target
        }

    private fun getTargets(): Flux<Target> =
        Flux.fromIterable(enqueueTargets)

    private fun findOrCreateSpaceForTarget(target: Target, spaceTemplate: Space): Mono<Space> =
        Flux.fromIterable(spaces)
            .flatMap { space -> space.canJoin(target).filter { it }.map { space } }
            .next()
            .switchIfEmpty(
                Mono.fromCallable {
                    Space(
                        id = "space:${spaces.size + 1}",
                        minJoins = spaceTemplate.minJoins,
                        maxJoins = spaceTemplate.maxJoins,
                        _targets = mutableListOf()
                    ).also { spaces += it }
                }
            )

    fun matchTargets(spaceTemplate: Space): Flux<Space> =
        getTargets().flatMap { target ->
            findOrCreateSpaceForTarget(target, spaceTemplate)
                .flatMap { space ->
                    space.addTarget(target).flatMap { added ->
                        when {
                            added -> removeTarget(target).thenReturn(space)
                            else -> Mono.just(space)
                        }
                    }
                }
        }
}

