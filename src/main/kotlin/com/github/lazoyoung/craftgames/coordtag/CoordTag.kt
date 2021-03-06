package com.github.lazoyoung.craftgames.coordtag

import com.github.lazoyoung.craftgames.game.Game
import org.bukkit.entity.Player
import java.math.BigDecimal

abstract class CoordTag(
        val game: Game,
        val mapID: String,
        val x: Double,
        val y: Double,
        val z: Double,
        val tagName: String?,
        val index: Int?
) {
    companion object Registry {
        /**
         * Gets all tags by reading through coordinate_tags.yml
         * @param mode is used to filter the result.
         * @return The list of tag names
         */
        fun getTagNames(game: Game, mode: TagMode? = null): MutableList<String> {
            if (mode == null) {
                val names = getTagNames(game, TagMode.BLOCK)
                names.addAll(getTagNames(game, TagMode.ENTITY))
                return names
            }

            return game.tagConfig.getConfigurationSection(mode.label)
                    ?.getKeys(false)
                    ?.toMutableList()
                    ?: mutableListOf()
        }

        /**
         * Gets all captures(coordinates) inside the sorted tags. A capture is fundamental unit of CoordinateTag.
         * Each parameter is like a filter sorting out captures.
         * @param mode Filter out all except the ones matching this tag mode.
         * @param tagName Filter out all except the ones matching this tag name.
         * @param mapID Filter out all except the ones inside the given map.
         * @return A list of captures inside the sorted tags.
         */
        fun getCaptures(game: Game, mode: TagMode? = null, tagName: String? = null, mapID: String? = null): List<CoordTag> {
            if (mode == null)
                return getCaptures(game, TagMode.BLOCK, tagName, mapID).plus(getCaptures(game, TagMode.ENTITY, tagName, mapID))

            if (tagName == null)
                return getTagNames(game, mode).flatMap { getCaptures(game, mode, it, mapID) }

            if (mapID == null)
                return game.map.getMapList().flatMap { getCaptures(game, mode, tagName, it) }

            return game.tagConfig.getStringList(getKey(mode, tagName, mapID)).mapIndexed { index, stream ->
                deserialize(game, mapID, mode, tagName, index, stream)
            }
        }

        fun getTagMode(game: Game, tagName: String): TagMode? {
            return when {
                getTagNames(game, TagMode.ENTITY).contains(tagName) -> {
                    TagMode.ENTITY
                }
                getTagNames(game, TagMode.BLOCK).contains(tagName) -> {
                    TagMode.BLOCK
                }
                else -> {
                    null
                }
            }
        }

        fun removeTag(game: Game, name: String, mapID: String?): Boolean {
            val mode = getTagMode(game, name) ?: return false
            var key = mode.label.plus(".").plus(name)

            if (mapID != null)
                key = key.plus(".").plus(mapID)

            game.tagConfig.set(key, null)
            return true
        }

        internal fun getKey(mode: TagMode, name: String, mapID: String) : String {
            return mode.label.plus(".").plus(name).plus(".").plus(mapID)
        }

        private fun deserialize(game: Game, mapID: String, mode: TagMode, tagName: String, index: Int, stream: String): CoordTag {
            val arr = stream.split(',', ignoreCase = false, limit = 5).toTypedArray()
            val x = arr[0].toBigDecimal()
            val y = arr[1].toBigDecimal()
            val z = arr[2].toBigDecimal()
            val yaw: BigDecimal
            val pitch: BigDecimal

            if (mode == TagMode.ENTITY) {
                yaw = arr[3].toBigDecimal()
                pitch = arr[4].toBigDecimal()
                return EntityCapture(game, mapID, x.toDouble(), y.toDouble(), z.toDouble(),
                        yaw.toFloat(), pitch.toFloat(), tagName, index)
            }
            return BlockCapture(game, mapID, x.toInt(), y.toInt(), z.toInt(), tagName, index)
        }
    }

    /**
     * Save this instance to configuration. Remember to save it before you wipe up everything.
     *
     * @param tagName It can be omitted if this instance already have it.
     * @throws IllegalArgumentException Thrown if tagName is undefined
     */
    abstract fun saveCapture(tagName: String?)
    abstract fun serialize() : String
    abstract fun teleport(player: Player)
}