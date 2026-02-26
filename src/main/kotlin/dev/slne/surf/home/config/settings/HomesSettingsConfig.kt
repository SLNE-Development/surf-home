package dev.slne.surf.home.config.settings

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class HomesSettingsConfig(
    @Setting("max-homes-per-player")
    val maxHomesPerPlayer: Int = 3,

    @Setting("teleport-wait-time-seconds")
    val teleportCooldownSeconds: Int = 120,

    @Setting("teleport-delay-seconds")
    val waitTimeSeconds: Int = 5,

    @Setting("creation-delay-seconds")
    val creationCooldownSeconds: Int = 300
)