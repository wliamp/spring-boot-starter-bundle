package vn.chuot96.gamecore.queue

data class Battle(
    val id: String,
    val minPlayers: Int,
    val maxPlayers: Int,
    val players: MutableList<Player> = mutableListOf()
) {
    /**
     * Kiểm tra player có thể join battle này không dựa trên số lượng và criteria.
     */
    fun canJoin(player: Player): Boolean {
        if (players.size >= maxPlayers) return false

        if (players.isNotEmpty()) {
            val baseCriteria = players[0].criteria
            if (!baseCriteria.matches(player.criteria)) return false
        }
        return true
    }

    /**
     * Thêm player vào battle nếu được phép.
     */
    fun addPlayer(player: Player): Boolean {
        if (!canJoin(player)) return false
        players.add(player)
        return true
    }
}
