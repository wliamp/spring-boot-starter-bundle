package io.github.wliamp.agr.data

import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.just

data class Space(
    val id: String,
    val minJoins: Int,
    val maxJoins: Int,
    private val _targets: MutableList<Target> = mutableListOf()
) {
    val targets: List<Target> get() = _targets

    fun canJoin(target: Target): Mono<Boolean> =
        when {
            _targets.size >= maxJoins -> just(false)
            _targets.isEmpty() -> just(true)
            else -> _targets.first().criteria.matches(target.criteria)
        }

    fun addTarget(target: Target): Mono<Boolean> =
        canJoin(target).flatMap { canJoin ->
            when {
                canJoin -> {
                    _targets += target
                    just(true)
                }
                else -> just(false)
            }
        }
}
