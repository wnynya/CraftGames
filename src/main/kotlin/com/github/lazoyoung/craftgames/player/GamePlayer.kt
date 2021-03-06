package com.github.lazoyoung.craftgames.player

import com.github.lazoyoung.craftgames.exception.ConcurrentPlayerState
import com.github.lazoyoung.craftgames.game.Game
import org.bukkit.entity.Player

class GamePlayer private constructor(
        player: Player,
        game: Game
): PlayerData(player, game) {
    private val team: Any = TODO()
    private val point: Int = TODO()

    companion object {
        fun register(player: Player, game: Game): GamePlayer {
            if (get(player) != null)
                throw ConcurrentPlayerState(null)

            val instance = GamePlayer(player, game)
            registry[player.uniqueId] = instance
            return instance
        }
    }
}