package dev.slne.surf.home.commands.subcommands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.stringArgument
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.home.config.settings.settingsConfig
import dev.slne.surf.home.home.HomeCreateResult
import dev.slne.surf.home.home.HomeService
import dev.slne.surf.home.util.userContent
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import kotlin.time.Duration.Companion.seconds

fun CommandAPICommand.homeCreateCommand() = subcommand("create") {
    stringArgument("name")

    playerExecutor { player, arguments ->
        val name = arguments["name"] as String

        val result = HomeService.createHome(player.uniqueId, name, player.location)

        when (result) {
            is HomeCreateResult.Success -> {
                player.sendText {
                    appendSuccessPrefix()
                    success("Du hast erfolgreich das Home")
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
                    variableValue(settingsConfig.maxHomesPerPlayer)
                    appendSpace()
                    error("erreicht!")
                }
            }

            is HomeCreateResult.NameAlreadyUsed -> {
                player.sendText {
                    appendErrorPrefix()
                    error("Du hast bereits ein Home mit dem Namen")
                    appendSpace()
                    variableValue(result.homeName)
                    appendSpace()
                    error("erstellt!")
                }
            }

            is HomeCreateResult.NameTooLarge -> {
                player.sendText {
                    appendErrorPrefix()
                    error("Der angegebene Name")
                    appendSpace()
                    variableValue(result.homeName)
                    appendSpace()
                    error("überschreitet die Gesamtlänge von 16 Zeichen!")
                }
            }

            is HomeCreateResult.CooldownActive -> {
                player.sendText {
                    appendErrorPrefix()
                    error("Du kannst nur alle")
                    appendSpace()
                    variableValue(settingsConfig.creationCooldownSeconds.seconds.userContent())
                    appendSpace()
                    error("ein neues Home erstellen!")
                }
            }
        }
    }
}