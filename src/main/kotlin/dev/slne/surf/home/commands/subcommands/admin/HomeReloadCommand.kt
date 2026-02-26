package dev.slne.surf.home.commands.subcommands.admin

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.home.config.homes.HomeConfigHolder
import dev.slne.surf.home.config.settings.HomeSettingsConfigHolder
import dev.slne.surf.home.home.HomeService
import dev.slne.surf.home.permissions.Permissions
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText

fun CommandAPICommand.homeReloadCommand() = subcommand("reload") {
    withPermission(Permissions.COMMAND_HOME_RELOAD)

    anyExecutor { sender, _ ->
        HomeConfigHolder.reload()
        HomeService.registerHomes()

        HomeSettingsConfigHolder.reload()

        sender.sendText {
            appendSuccessPrefix()
            success("Die existierenden Homes sowie Einstellungen wurde aktualisiert!")

            appendNewSuccessPrefixedLine()
            success("Es wurden")
            appendSpace()
            variableValue(HomeService.homeCount)
            appendSpace()
            success("Homes neu geladen!")
        }
    }
}