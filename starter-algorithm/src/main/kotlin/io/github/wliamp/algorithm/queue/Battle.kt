package io.github.wliamp.algorithm.queue

data class Battle(
    val id: String,
    val minPlayers: Int,
    val maxPlayers: Int,
    val players: MutableList<Player> = mutableListOf()
) {
    fun canJoin(player: Player): Boolean {
        if (players.size >= maxPlayers) return false

        if (players.isNotEmpty()) {
            val baseCriteria = players[0].criteria
            if (!baseCriteria.matches(player.criteria)) return false
        }
        return true
    }

    fun addPlayer(player: Player): Boolean {
        if (!canJoin(player)) return false
        players.add(player)
        return true
    }
}
