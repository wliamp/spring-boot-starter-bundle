package io.github.wliamp.algorithm.queue

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.concurrent.CopyOnWriteArrayList

class MatchMaker {
    private val waitingPlayers = CopyOnWriteArrayList<Player>()
    private val battles = CopyOnWriteArrayList<Battle>()

    private fun addPlayer(player: Player): Mono<Player> =
        Mono.fromCallable {
            waitingPlayers.add(player)
            player
        }

    private fun removePlayer(player: Player): Mono<Void> =
        Mono.fromRunnable {
            waitingPlayers.remove(player)
        }

    private fun getPlayers(): Flux<Player> =
        Flux.fromIterable(waitingPlayers)

    private fun findOrCreateBattleForPlayer(player: Player, battleTemplate: Battle): Mono<Battle> =
        Flux.fromIterable(battles)
            .filter { it.canJoin(player) }
            .next()
            .switchIfEmpty(
                Mono.fromCallable {
                    val newBattle = Battle(
                        id = generateBattleId(),
                        minPlayers = battleTemplate.minPlayers,
                        maxPlayers = battleTemplate.maxPlayers,
                        players = mutableListOf()
                    )
                    battles.add(newBattle)
                    newBattle
                }
            )

    private fun matchPlayers(battleTemplate: Battle): Flux<Battle> =
        getPlayers()
            .flatMap { player ->
                findOrCreateBattleForPlayer(player, battleTemplate)
                    .flatMap { battle ->
                        if (battle.addPlayer(player)) removePlayer(player).thenReturn(battle) else Mono.just(battle)
                    }
            }

    fun enqueue(player: Player, battleTemplate: Battle): Mono<Battle> =
        addPlayer(player)
            .thenMany(matchPlayers(battleTemplate))
            .last()

    private fun generateBattleId(): String = "battle-${battles.size + 1}"
}
