package dev.slne.surf.home.home

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.google.auto.service.AutoService
import dev.slne.surf.home.config.homes.HomeConfigHolder
import dev.slne.surf.home.config.homes.homeConfig
import dev.slne.surf.home.config.settings.settingsConfig
import dev.slne.surf.home.plugin
import dev.slne.surf.home.util.userContent
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.util.freeze
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.kyori.adventure.util.Services
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import net.kyori.adventure.sound.Sound as AdventureSound
import org.bukkit.Sound as BukkitSound

sealed class HomeCreateResult {
    data class Success(val home: Home) : HomeCreateResult()
    object LimitReached : HomeCreateResult()
    data class NameAlreadyUsed(val homeName: String) : HomeCreateResult()
    data class NameTooLarge(val homeName: String) : HomeCreateResult()
    object CooldownActive : HomeCreateResult()
}

@AutoService(HomeService::class)
class HomeServiceImpl : HomeService, Services.Fallback {
    private val homesMap = ConcurrentHashMap<UUID, Home>()
    override val homes get() = ObjectArrayList(homesMap.values).freeze()

    private val lastCreations = Caffeine.newBuilder()
        .expireAfterWrite(settingsConfig.creationCooldownSeconds.seconds.toJavaDuration())
        .build<UUID, Unit>()

    private val teleportCooldowns = Caffeine.newBuilder()
        .expireAfterWrite(settingsConfig.teleportCooldownSeconds.seconds.toJavaDuration())
        .build<UUID, Unit>()

    private val executions = Caffeine.newBuilder()
        .expireAfterWrite(settingsConfig.waitTimeSeconds.seconds.toJavaDuration())
        .removalListener<UUID, Job> { uuid, job, cause ->
            job?.cancel()
        }
        .build<UUID, Job>()


    override val homeCount get() = homesMap.size

    override fun registerHomes() {
        homesMap.clear()

        homeConfig.homes.forEach { home ->
            homesMap[home.id] = home
        }
    }

    override fun registerHome(home: Home) {
        homesMap[home.id] = home

        homeConfig.apply {
            homes.add(home)
        }
        HomeConfigHolder.save()
    }

    override fun unregisterHome(home: Home) {
        homesMap.remove(home.id)

        homeConfig.apply {
            homes.remove(home)
        }
        HomeConfigHolder.save()
    }

    override fun createHome(ownerId: UUID, name: String, location: Location): HomeCreateResult {
        if (lastCreations.getIfPresent(ownerId) != null) {
            return HomeCreateResult.CooldownActive
        }

        val existingHomes = getHomesOf(ownerId)
        if (existingHomes.size >= settingsConfig.maxHomesPerPlayer) {
            return HomeCreateResult.LimitReached
        }

        if (existingHomes.any { it.name.equals(name, ignoreCase = true) }) {
            return HomeCreateResult.NameAlreadyUsed(name)
        }

        if(name.length > 16){
            return HomeCreateResult.NameTooLarge(name)
        }

        val id = generateUnusedId()
        val home = home(id, name, ownerId, location)

        registerHome(home)
        lastCreations.put(ownerId, Unit)

        return HomeCreateResult.Success(home)
    }

    override fun deleteHome(homeId: UUID) {
        val home = getHomeById(homeId) ?: return
        unregisterHome(home)
    }

    override suspend fun teleportPlayerToHome(player: Player, home: Home) {
        val uuid = player.uniqueId

        if (teleportCooldowns.getIfPresent(uuid) != null) {
            player.sendText {
                appendErrorPrefix()
                error("Du kannst dich nur alle")
                appendSpace()
                variableValue(settingsConfig.teleportCooldownSeconds.seconds.userContent())
                appendSpace()
                error("teleportieren.")
            }
            return
        }

        player.sendText {
            appendSuccessPrefix()
            success("Du wirst in")
            appendSpace()
            variableValue(settingsConfig.waitTimeSeconds.seconds.userContent())
            appendSpace()
            success("zu")
            appendSpace()
            variableValue(home.name)
            appendSpace()
            success("teleportiert")

            appendNewSuccessPrefixedLine()
            success("Bitte bewege dich nicht und erhalte keinen Schaden!")
        }

        executions.invalidate(uuid)

        val startLocation = withContext(plugin.entityDispatcher(player)) {
            player.location.clone()
        }
        val startHealth = withContext(plugin.entityDispatcher(player)) {
            player.health
        }

        val job = plugin.launch(plugin.entityDispatcher(player)) {
            for (secondsLeft in settingsConfig.waitTimeSeconds.seconds.inWholeSeconds downTo 0) {
                if (player.location.distanceSquared(startLocation) > 0.1) {
                    player.sendText {
                        appendErrorPrefix()
                        error("Die Teleportation zu")
                        appendSpace()
                        variableValue(home.name)
                        appendSpace()
                        error("wurde abgebrochen, da du dich bewegt hast!")
                    }
                    player.playFailSound()
                    executions.invalidate(uuid)
                    return@launch
                }
                if (player.health != startHealth) {
                    player.sendText {
                        appendErrorPrefix()
                        error("Die Teleportation zu")
                        appendSpace()
                        variableValue(home.name)
                        appendSpace()
                        error("wurde abgebrochen, weil du Schaden erhalten hast hast!")
                    }
                    player.playFailSound()
                    executions.invalidate(uuid)
                    return@launch
                }

                player.sendRemainingTimeActionBar(home.name, secondsLeft.seconds)
                player.playTeleportSound(false)
                delay(1.seconds)
            }

            player.teleportAsync(home.location).thenAccept {
                teleportCooldowns.put(uuid, Unit)
                player.playTeleportSound(true)
                player.sendText {
                    appendSuccessPrefix()
                    success("Du wurdest zum Home")
                    appendSpace()
                    variableValue(home.name)
                    appendSpace()
                    success("teleportiert.")
                }
            }
            executions.invalidate(uuid)
        }

        executions.put(uuid, job)
    }

    private fun Player.sendRemainingTimeActionBar(homeName: String, timeLeft: Duration) {
        val content =
            buildText {
                primary("»")
                appendSpace()
                variableValue(homeName)

                if (timeLeft > Duration.ZERO) {
                    appendSpace()
                    spacer("|")
                    appendSpace()
                    variableValue(timeLeft.userContent())
                }

                appendSpace()
                primary("«")

            }
        sendActionBar(content)
    }

    private fun Player.playTeleportSound(teleport: Boolean) = playSound(true) {
        if (teleport) {
            type(BukkitSound.ENTITY_ENDERMAN_TELEPORT)
        } else {
            type(BukkitSound.BLOCK_NOTE_BLOCK_PLING)
        }
        volume(.5f)
        source(AdventureSound.Source.PLAYER)
    }

    private fun Player.playFailSound() = playSound(true) {
        type(BukkitSound.BLOCK_ANVIL_DESTROY)
        volume(.5f)
        source(AdventureSound.Source.PLAYER)
    }

    override fun getHomeById(id: UUID): Home? = homesMap[id]

    override fun getHomesOf(ownerId: UUID): List<Home> = homesMap.values.filter { it.ownerUuid == ownerId }

    override fun getHomeByName(ownerId: UUID, name: String): Home? =
        homesMap.values.firstOrNull {
            it.ownerUuid == ownerId && it.name.equals(name, ignoreCase = true)
        }

    override fun generateUnusedId(): UUID {
        var id: UUID
        do {
            id = UUID.randomUUID()
        } while (homesMap.containsKey(id))
        return id
    }

    override fun saveHomes() {
        HomeConfigHolder.save()
    }
}