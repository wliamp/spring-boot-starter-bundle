package org.wliamp.gameplay.queue

class MatchMaker {
    private val waitingPlayers = mutableListOf<Player>()

    private fun addPlayer(player: Player) {
        waitingPlayers.add(player)
    }

    private fun removePlayer(player: Player) {
        waitingPlayers.remove(player)
    }

    private fun getPlayers(): List<Player> = waitingPlayers.toList()

    private val battles = mutableListOf<Battle>()

    /**
     * Tìm battle phù hợp cho player, hoặc tạo mới dựa trên battleTemplate nếu không có.
     */
    private fun findOrCreateBattleForPlayer(player: Player, battleTemplate: Battle): Battle {
        val battle = battles.firstOrNull { it.canJoin(player) } ?: run {
            val newBattle = Battle(
                id = generateBattleId(),
                minPlayers = battleTemplate.minPlayers,
                maxPlayers = battleTemplate.maxPlayers,
                players = mutableListOf()
            )
            battles.add(newBattle)
            newBattle
        }
        return battle
    }

    /**
     * Match tất cả player đang chờ với battle template truyền vào.
     */
    private fun matchPlayers(battleTemplate: Battle) {
        for (player in getPlayers()) {
            val battle = findOrCreateBattleForPlayer(player, battleTemplate)
            if (battle.addPlayer(player)) {
                removePlayer(player)
            }
        }
    }

    fun enqueue(player: Player, battleTemplate: Battle) {
        addPlayer(player)
        matchPlayers(battleTemplate)
    }

    private fun generateBattleId(): String = "battle-${battles.size + 1}"
}
