package dev.slne.surf.home.config.settings

import dev.slne.surf.home.plugin
import dev.slne.surf.surfapi.core.api.config.createSpongeYmlConfig
import dev.slne.surf.surfapi.core.api.config.manager.SpongeConfigManager
import dev.slne.surf.surfapi.core.api.config.surfConfigApi

object HomeSettingsConfigHolder {
    private val manager: SpongeConfigManager<HomesSettingsConfig>

    init {
        surfConfigApi.createSpongeYmlConfig<HomesSettingsConfig>(plugin.dataPath, "config.yml")
        manager = surfConfigApi.getSpongeConfigManagerForConfig(HomesSettingsConfig::class.java)
    }

    val config: HomesSettingsConfig get() = manager.config

    fun save() {
        manager.save()
    }

    fun reload() {
        manager.reloadFromFile()
    }
}

val settingsConfig get() = HomeSettingsConfigHolder.config