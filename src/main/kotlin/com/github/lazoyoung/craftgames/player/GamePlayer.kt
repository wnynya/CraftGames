package com.github.lazoyoung.craftgames.player

import com.github.lazoyoung.craftgames.exception.ConcurrentPlayerState
import com.github.lazoyoung.craftgames.game.Game
import com.github.lazoyoung.craftgames.game.GameFactory
import java.util.*

class GamePlayer(
        val playerID: UUID,
        val gameID: Int
) {
    private val team: Any = TODO()
    private val point: Int = TODO()

    companion object {
        private val registry = HashMap<UUID, GamePlayer>()

        fun from(playerID: UUID, game: Game): GamePlayer {
            if (PlayerState.get(playerID) != PlayerState.NONE)
                throw ConcurrentPlayerState(null)

            var instance = registry[playerID]

            if (instance == null) {
                instance = GamePlayer(playerID, game.id)
                registry[playerID] = instance
            }
            return instance
        }
    }

    fun getGame() : Game? {
        return GameFactory.findByID(gameID)
    }
}