package com.github.lazoyoung.craftgames.player

import java.util.*
import kotlin.collections.HashMap

enum class PlayerState {
    NONE, PLAYING, WATCHING, EDITING;

    companion object {
        val state = HashMap<UUID, PlayerState>()

        fun get(playerId: UUID): PlayerState {
            return state[playerId] ?: NONE
        }
    }
}