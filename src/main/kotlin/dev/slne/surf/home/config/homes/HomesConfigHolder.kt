package dev.slne.surf.home.config.homes

import dev.slne.surf.home.plugin
import dev.slne.surf.surfapi.core.api.config.createSpongeYmlConfig
import dev.slne.surf.surfapi.core.api.config.manager.SpongeConfigManager
import dev.slne.surf.surfapi.core.api.config.surfConfigApi

object HomeConfigHolder {
    private val manager: SpongeConfigManager<HomeConfig>

    init {
        surfConfigApi.createSpongeYmlConfig<HomeConfig>(plugin.dataPath, "homes.yml")
        manager = surfConfigApi.getSpongeConfigManagerForConfig(HomeConfig::class.java)
    }

    val config: HomeConfig get() = manager.config

    fun save() {
        manager.save()
    }

    fun reload() {
        manager.reloadFromFile()
    }
}

val homeConfig get() = HomeConfigHolder.config