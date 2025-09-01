package io.github.wliamp.agr

import io.github.wliamp.agr.data.Space
import io.github.wliamp.agr.data.Target
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.concurrent.CopyOnWriteArrayList

class MatchMaker {
    private val enqueueTargets = CopyOnWriteArrayList<Target>()
    private val spaces = CopyOnWriteArrayList<Space>()

    private fun addTarget(target: Target): Mono<Target> = Mono.fromCallable {
        enqueueTargets += target;
        target
    }

    private fun removeTarget(target: Target): Mono<Void> = Mono.fromRunnable {
        enqueueTargets -= target
    }

    private fun getTargets(): Flux<Target> = Flux.fromIterable(enqueueTargets)

    private fun findOrCreateSpaceForTarget(target: Target, spaceTemplate: Space): Mono<Space> =
        Flux.fromIterable(spaces)
            .filter { it.canJoin(target) }
            .next()
            .switchIfEmpty(
                Mono.fromCallable {
                    Space(
                        id = "space:${spaces.size + 1}",
                        minJoins = spaceTemplate.minJoins,
                        maxJoins = spaceTemplate.maxJoins,
                        _targets = mutableListOf()
                    ).also { spaces += it }
                })

    private fun matchTargets(spaceTemplate: Space): Flux<Space> =
        getTargets().flatMap { target ->
            findOrCreateSpaceForTarget(target, spaceTemplate)
                .flatMap { space ->
                    space.takeIf { it.addTarget(target) }
                        ?.let { removeTarget(target).thenReturn(it) }
                        ?: Mono.just(space)
                }
        }

    fun enqueue(target: Target, spaceTemplate: Space): Mono<Space> =
        addTarget(target)
            .thenMany(matchTargets(spaceTemplate))
            .last()
}
