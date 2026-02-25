package dev.slne.surf.home.commands.subcommands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.stringArgument
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.home.home.HOME_CREATION_COOLDOWN
import dev.slne.surf.home.home.HomeCreateResult
import dev.slne.surf.home.home.HomeService
import dev.slne.surf.home.home.MAX_HOMES_PER_PLAYER
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText

fun CommandAPICommand.homeCreateCommand() = subcommand("create") {
    stringArgument("name")

    playerExecutor { player, arguments ->
        val name = arguments["name"] as String

        val result = HomeService.createHome(player.uniqueId, name, player.location)

        when (result) {
            is HomeCreateResult.Success -> {
                player.sendText {
                    appendSuccessPrefix()
                    success("Du hast erfolgreich das Zuhause")
                    appendSpace()
                    variableValue(result.home.name)
                    appendSpace()
                    success("erstellt!")
                }
            }

            is HomeCreateResult.LimitReached -> {
                player.sendText {
                    appendErrorPrefix()
                    error("Du hast das maximale Limit von")
                    appendSpace()
                    variableValue(MAX_HOMES_PER_PLAYER)
                    appendSpace()
                    error("erreicht!")
                }
            }

            is HomeCreateResult.NameAlreadyUsed -> {
                player.sendText {
                    appendErrorPrefix()
                    error("Du hast bereits ein Zuhause mit dem Namen")
                    appendSpace()
                    variableValue(result.homeName)
                    appendSpace()
                    error("erstellt!")
                }
            }

            is HomeCreateResult.CooldownActive -> {
                val minutes = HOME_CREATION_COOLDOWN.inWholeMinutes
                player.sendText {
                    appendErrorPrefix()
                    error("Du kannst nur alle")
                    appendSpace()
                    variableValue("$minutes Minuten")
                    appendSpace()
                    error("ein neues Zuhause erstellen!")
                }
            }
        }
    }
}