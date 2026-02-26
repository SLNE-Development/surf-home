package dev.slne.surf.home.commands.subcommands.admin

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.home.config.homes.HomeConfigHolder
import dev.slne.surf.home.config.settings.HomeSettingsConfigHolder
import dev.slne.surf.home.config.settings.settingsConfig
import dev.slne.surf.home.home.HomeService
import dev.slne.surf.home.permissions.Permissions
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText

fun CommandAPICommand.homeSettingsCommand() = subcommand("settings") {
    withPermission(Permissions.COMMAND_HOME_RELOAD)

    anyExecutor { sender, _ ->
        val config = settingsConfig

        sender.sendText {
            appendSuccessPrefix()
            success("Aktuelle Home-Einstellungen:")
            appendNewline()

            appendNewSuccessPrefixedLine()
            spacer("Max. Homes pro Spieler:")
            appendSpace()
            variableValue(config.maxHomesPerPlayer.toString())

            appendNewSuccessPrefixedLine()
            spacer("Teleport-Cooldown:")
            appendSpace()
            variableValue("${config.teleportCooldownSeconds}s")

            appendNewSuccessPrefixedLine()
            spacer("Erstellungs-Delay:")
            appendSpace()
            variableValue("${config.creationCooldownSeconds}s")

            appendNewSuccessPrefixedLine()
            spacer("Wartezeit (Teleport):")
            appendSpace()
            variableValue("${config.waitTimeSeconds}s")
        }
    }
}