package dev.slne.surf.home.permissions

import dev.slne.surf.surfapi.bukkit.api.permission.PermissionRegistry

object Permissions : PermissionRegistry() {
    private const val PREFIX = "surf.home"
    private const val COMMAND_PREFIX = "$PREFIX.command"

    val COMMAND_HOME_RELOAD = create("$COMMAND_PREFIX.reload")
    val COMMAND_HOME_GENERIC = create("$COMMAND_PREFIX.home")
}