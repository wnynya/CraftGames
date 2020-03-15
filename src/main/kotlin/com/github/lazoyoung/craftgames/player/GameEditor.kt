package com.github.lazoyoung.craftgames.player

import com.github.lazoyoung.craftgames.exception.ConcurrentPlayerState
import com.github.lazoyoung.craftgames.game.Game
import com.github.lazoyoung.craftgames.game.GameFactory
import java.util.*

class GameEditor(
        val playerID: UUID,
        val gameID: Int
) {
    companion object {
        private val registry = HashMap<UUID, GameEditor>()

        fun from(playerID: UUID, game: Game): GameEditor {
            if (PlayerState.get(playerID) != PlayerState.NONE)
                throw ConcurrentPlayerState(null)

            var instance = registry[playerID]

            if (instance == null) {
                instance = GameEditor(playerID, game.id)
                registry[playerID] = instance
            }
            return instance
        }
    }

    fun getGame() : Game? {
        return GameFactory.findByID(gameID)
    }
}