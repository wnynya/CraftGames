package com.github.lazoyoung.craftgames

import com.github.lazoyoung.craftgames.command.CoordtagCommand
import com.github.lazoyoung.craftgames.command.GameCommand
import org.bukkit.Bukkit
import org.bukkit.command.CommandExecutor
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path

class Main : JavaPlugin(), CommandExecutor {

    companion object {
        lateinit var config: FileConfiguration
            private set
        lateinit var instance: Main
            private set
        lateinit var charset: Charset
            private set
    }

    override fun onEnable() {
        val gameCmd = getCommand("game")!!
        val ctCmd = getCommand("coord")!!
        val gameExecutor = GameCommand()
        val ctExecutor = CoordtagCommand()
        instance = this

        loadConfig()
        loadAsset()
        gameCmd.setExecutor(gameExecutor)
        ctCmd.setExecutor(ctExecutor)
        gameCmd.tabCompleter = gameExecutor
        ctCmd.tabCompleter = ctExecutor
        Bukkit.getPluginManager().registerEvents(EventListener(), this)
    }

    private fun loadConfig() {
        saveDefaultConfig()
        config.options().copyDefaults(true)
        Main.config = config
        charset = Charset.forName(config.getString("file-encoding"))
    }

    private fun loadAsset() {
        val root = dataFolder
        val sys: FileSystem
        val source: Path
        val target: Path

        if (config.getBoolean("install-sample")) {
            logger.info("Installing sample files...")

            try {
                sys = FileSystems.newFileSystem(file.toPath(), classLoader)
                source = sys.getPath("Sample")
                target = root.toPath()
            } catch (e: Exception) {
                e.printStackTrace()
                logger.severe("Unable to read jar files.")
                return
            }

            try {
                FileUtil(logger).cloneFileTree(source, target)
            } catch (e: SecurityException) {
                e.printStackTrace()
                logger.severe("Access denied! Unable to install sample files.")
                return
            } catch (e: IOException) {
                e.printStackTrace()
                logger.severe("Error occurred! Unable to install sample files.")
                return
            }

            logger.info("Sample files have been installed!")
            config.set("install-sample", false)
            saveConfig()
        }
    }
}