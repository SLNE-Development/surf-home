package dev.slne.surf.home.commands

import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.slne.surf.home.commands.subcommands.admin.homeReloadCommand
import dev.slne.surf.home.commands.subcommands.admin.homeSettingsCommand
import dev.slne.surf.home.commands.subcommands.homeCreateCommand
import dev.slne.surf.home.commands.subcommands.homeDeleteCommand
import dev.slne.surf.home.commands.subcommands.homeListCommand
import dev.slne.surf.home.commands.subcommands.homeTeleportCommand
import dev.slne.surf.home.permissions.Permissions

fun homeCommand() = commandAPICommand("home") {
    withPermission(Permissions.COMMAND_HOME_GENERIC)

    homeCreateCommand()
    homeDeleteCommand()
    homeListCommand()
    homeTeleportCommand()

    homeReloadCommand()
    homeSettingsCommand()
}
