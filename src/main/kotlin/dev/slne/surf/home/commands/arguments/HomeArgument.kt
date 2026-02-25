package dev.slne.surf.home.commands.arguments

import com.github.shynixn.mccoroutine.folia.scope
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.slne.surf.home.home.Home
import dev.slne.surf.home.home.HomeService
import dev.slne.surf.home.plugin
import dev.slne.surf.surfapi.core.api.messages.adventure.uuid
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.future.future
import org.bukkit.entity.Player


const val HOME_ARGUMENT_NODE_NAME = "home"

@OptIn(DelicateCoroutinesApi::class)
class HomeArgument(
    nodeName: String = HOME_ARGUMENT_NODE_NAME
) : CustomArgument<Home?, String>(
    StringArgument(nodeName),
    { info ->
        val homeName = info.currentInput
        val playerUuid = info.sender.uuid()

        HomeService.getHomeByName(playerUuid, homeName)
    }
) {
    init {
        replaceSuggestions(ArgumentSuggestions.stringCollectionAsync { info ->
            plugin.scope.future {
                val player = info.sender as? Player ?: return@future emptyList()

                HomeService.getHomesOf(player.uniqueId).map { it.name }
            }
        })
    }
}

fun CommandAPICommand.homeArgument(
    nodeName: String = HOME_ARGUMENT_NODE_NAME,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandAPICommand = withArguments(
    HomeArgument(nodeName).setOptional(optional).apply(block)
)