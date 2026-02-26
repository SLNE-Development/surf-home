package dev.slne.surf.home

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.home.commands.homeCommand
import dev.slne.surf.home.home.HomeService
import org.bukkit.plugin.java.JavaPlugin

class PaperMain : SuspendingJavaPlugin() {

    override suspend fun onEnableAsync() {
        homeCommand()
        HomeService.registerHomes()
    }

    override fun onDisable() {
    }
}

val plugin: PaperMain get() = JavaPlugin.getPlugin(PaperMain::class.java)

