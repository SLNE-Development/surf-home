package dev.slne.surf.home.home

import dev.slne.surf.surfapi.core.api.config.SpongeYmlConfigClass
import dev.slne.surf.home.plugin
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class HomeConfig(
    val homes: MutableList<Home>
) {
    companion object : SpongeYmlConfigClass<HomeConfig>(
        HomeConfig::class.java,
        plugin.dataPath,
        "homes.yml"
    )
}
