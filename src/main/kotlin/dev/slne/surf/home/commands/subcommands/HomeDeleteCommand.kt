package dev.slne.surf.home.commands.subcommands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.home.commands.arguments.homeArgument
import dev.slne.surf.home.home.Home
import dev.slne.surf.home.home.HomeService
import dev.slne.surf.home.home.home
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import org.bukkit.entity.Player

fun CommandAPICommand.homeDeleteCommand() = subcommand("delete") {
    homeArgument()

    playerExecutor { player, arguments ->
        val home: Home? by arguments

        if(home == null){
            player.sendText {
                appendErrorPrefix()
                error("Das angegebene Home konnte nicht gefunden werden!")
            }
            return@playerExecutor
        }

        HomeService.deleteHome(home!!.id)

        player.sendText {
            appendSuccessPrefix()
            success("Das angegebene Home")
            appendSpace()
            variableValue(home!!.name)
            appendSpace()
            success("wurde gelöscht.")
        }
    }
}