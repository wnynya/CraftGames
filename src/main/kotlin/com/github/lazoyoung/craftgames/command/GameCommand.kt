package com.github.lazoyoung.craftgames.command

import com.github.lazoyoung.craftgames.Main
import com.github.lazoyoung.craftgames.exception.FaultyConfiguration
import com.github.lazoyoung.craftgames.exception.GameNotFound
import com.github.lazoyoung.craftgames.exception.MapNotFound
import com.github.lazoyoung.craftgames.exception.ScriptEngineNotFound
import com.github.lazoyoung.craftgames.game.Game
import com.github.lazoyoung.craftgames.game.GameFactory
import com.github.lazoyoung.craftgames.script.ScriptBase
import groovy.lang.GroovyRuntimeException
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import java.util.function.Consumer

class GameCommand : TabExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val game: Game?

        if (command.name != "game")
            return true

        if (args.isEmpty()) {
            sender.sendMessage(arrayOf(
                    "/game start <name> [mapID]",
                    "/game stop <id>",
                    "/game script <name> <scriptID> execute"
            ))
            return true
        }

        if (args.size == 1)
            return false

        if (args[0].equals("start", true)) {
            if (args.size < 3)
                return false

            val name = args[1]
            game = getGame(name, sender)

            if (game == null)
                return true

            try {
                game.map.generate(args[2], Consumer{
                    if (it == null) {
                        sender.sendMessage("Started $name.")
                        return@Consumer
                    }

                    if (sender is Player) {
                        // TODO This function will be replaced by Game#start()
                        sender.teleport(it.spawnLocation)
                    }
                    sender.sendMessage("Started $name with map: ${game.map.mapID}")
                })
            } catch (e: RuntimeException) {
                e.printStackTrace()
                sender.sendMessage("Unable to load map.")
            } catch (e: FaultyConfiguration) {
                e.printStackTrace()
                sender.sendMessage("Unable to load map.")
            } catch (e: MapNotFound) {
                sender.sendMessage(e.message!!)
            }
        }
        else if (args[0].equals("stop", true)) {
            val id = args[1].toIntOrNull()

            if (args.size < 2 || id == null)
                return false

            try {
                if (GameFactory.findByID(id)!!.stop()) {
                    sender.sendMessage("The game has been stopped.")
                } else {
                    sender.sendMessage("Unexpected error.")
                }
            } catch (e: NullPointerException) {
                sender.sendMessage("No game is running with id $id.")
                return true
            }
        }
        else if (args[0].equals("script", true)) {
            if (args.size < 4)
                return false

            val name = args[1]
            game = getGame(name, sender)

            if (game == null)
                return true

            val scriptID: String = args[2]
            val script: ScriptBase? = game.scriptReg[scriptID]

            if (script == null) {
                sender.sendMessage("That script ($scriptID) does not exist.")
                return true
            }

            if (args[3] == "execute") {
                try {
                    script.parse()
                    script.execute()
                    sender.sendMessage("Script $scriptID has been executed.")
                } catch (e: GroovyRuntimeException) {
                    sender.sendMessage("Compilation error: ${e.message}")
                    e.printStackTrace()
                } catch (e: ScriptEngineNotFound) {
                    sender.sendMessage(e.message)
                }
            }
        } else return false

        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>)
            : MutableList<String>? {
        if (args.isEmpty())
            return null

        if (args.size == 1)
            return arrayListOf("start", "stop", "script")

        fun getGames() : MutableList<String> {
            return Main.config.getConfigurationSection("games")?.getKeys(false)!!.toMutableList()
        }

        when {
            args[0].equals("start", true) -> {
                return when (args.size) {
                    2 -> getGames()
                    3 -> {
                        val reg = GameFactory.getDummy(args[1]).map.mapRegistry
                        reg.mapNotNull { it["id"] as String? }.toMutableList()
                    }
                    else -> null
                }
            }
            args[0].equals("stop", true) -> {
                return when (args.size) {
                    2 -> GameFactory.find().map { it.id.toString() }.toMutableList()
                    else -> null
                }
            }
            args[0].equals("script", true) -> {
                when (args.size) {
                    2 -> return getGames()
                    3 -> {
                        return try {
                            GameFactory.getDummy(args[1]).scriptReg.keys.toMutableList()
                        } catch (e: Exception) {
                            null
                        }
                    }
                    4 -> return arrayListOf("execute")
                }
            }
        }
        return null
    }

    private fun getGame(name: String, sender: CommandSender): Game? {
        var game: Game? = null
        try {
            game = GameFactory.openNew(name)
        } catch (e: GameNotFound) {
            sender.sendMessage(e.message!!)
        } catch (e: FaultyConfiguration) {
            e.printStackTrace()
            sender.sendMessage(e.message!!)
        } catch (e: RuntimeException) {
            e.printStackTrace()
            sender.sendMessage(e.message!!)
        } catch (e: ScriptEngineNotFound) {
            e.printStackTrace()
            sender.sendMessage(e.message)
        }
        return game
    }

}