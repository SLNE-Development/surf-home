package dev.slne.surf.home.home

import dev.slne.surf.home.plugin
import dev.slne.surf.surfapi.core.api.config.SpongeYmlConfigClass
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class HomeConfig(
    val homes: MutableList<Home> = mutableListOf()
) {
    companion object : SpongeYmlConfigClass<HomeConfig>(
        HomeConfig::class.java,
        plugin.dataPath,
        "homes.yml"
    )
}