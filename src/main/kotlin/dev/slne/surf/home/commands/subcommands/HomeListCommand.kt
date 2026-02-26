package dev.slne.surf.home.commands.subcommands

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.home.home.Home
import dev.slne.surf.home.home.HomeService
import dev.slne.surf.home.plugin
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.clickCallback
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.messages.pagination.Pagination
import dev.slne.surf.surfapi.core.api.util.dateTimeFormatter
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player

private fun pagination(homeCount: Int) = Pagination<Home> {
    title {
        primary("Deine Homes".toSmallCaps(), TextDecoration.BOLD)
        appendSpace()
        spacer("($homeCount Stück)")
    }

    rowRenderer { home, _ ->
        listOf(
            buildText {
                spacer(">")
                appendSpace()
                variableValue(home.name)
                appendSpace()
                spacer("(${dateTimeFormatter.format(home.createdAt)})")

                hoverEvent(buildText {
                    success("Klicke, um dich zum Home")
                    appendSpace()
                    variableValue(home.name)
                    appendSpace()
                    success("zu teleportieren.")
                })
                clickCallback {
                    val player = it as? Player ?: return@clickCallback
                    plugin.launch {
                        HomeService.teleportPlayerToHome(player, home)
                    }
                }
            }
        )
    }
}

fun CommandAPICommand.homeListCommand() = subcommand("list") {
    playerExecutor { player, _ ->
        val homes = HomeService.getHomesOf(player.uniqueId)

        if (homes.isEmpty()) {
            player.sendText {
                appendErrorPrefix()
                error("Du besitzt keine Homes!")
            }
            return@playerExecutor
        }

        player.sendText {
            appendNewline()
            append(pagination(homes.size).renderComponent(homes))
        }
    }
}