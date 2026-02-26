package dev.slne.surf.home.config.homes

import dev.slne.surf.home.home.Home
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class HomeConfig(
    val homes: MutableList<Home>
)