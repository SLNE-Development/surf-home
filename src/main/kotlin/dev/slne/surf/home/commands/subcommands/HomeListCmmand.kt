package dev.slne.surf.home.commands.subcommands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.home.home.Home
import dev.slne.surf.home.home.HomeService
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.clickRunsCommand
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.messages.pagination.Pagination
import dev.slne.surf.surfapi.core.api.util.dateTimeFormatter
import net.kyori.adventure.text.format.TextDecoration

private val pagination = Pagination<Home> {
    title {
        primary("Deine Zuhauses".toSmallCaps(), TextDecoration.BOLD)
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
                    success("Klicke, um dich zum Zuhause")
                    appendSpace()
                    variableValue(home.name)
                    appendSpace()
                    success("zu teleportieren.")
                })
                clickRunsCommand("/homes teleport ${home.name}")
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
                error("Du besitzt kein Zuhause!")
            }
            return@playerExecutor
        }

        player.sendText {
            appendNewline()
            append(pagination.renderComponent(homes))
        }
    }
}