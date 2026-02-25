package dev.slne.surf.home.home

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.google.auto.service.AutoService
import dev.slne.surf.home.plugin
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
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import net.kyori.adventure.sound.Sound as AdventureSound
import org.bukkit.Sound as BukkitSound

const val MAX_HOMES_PER_PLAYER = 3
val TELEPORT_COOLDOWN = 5.minutes
val HOME_CREATION_COOLDOWN = 5.minutes
val WAIT_TIME = 5.seconds

sealed class HomeCreateResult {
    data class Success(val home: Home) : HomeCreateResult()
    object LimitReached : HomeCreateResult()
    data class NameAlreadyUsed(val homeName: String) : HomeCreateResult()
    object CooldownActive : HomeCreateResult()
}

@AutoService(HomeService::class)
class HomeServiceImpl : HomeService, Services.Fallback {
    private val homesMap = ConcurrentHashMap<UUID, Home>()
    override val homes get() = ObjectArrayList(homesMap.values).freeze()

    private val lastCreations = Caffeine.newBuilder()
        .expireAfterWrite(HOME_CREATION_COOLDOWN.toJavaDuration())
        .build<UUID, Unit>()

    private val teleportCooldowns = Caffeine.newBuilder()
        .expireAfterWrite(TELEPORT_COOLDOWN.toJavaDuration())
        .build<UUID, Unit>()

    private val executions = Caffeine.newBuilder()
        .expireAfterWrite(WAIT_TIME.toJavaDuration())
        .removalListener<UUID, Job> { uuid, job, cause ->
            job?.cancel()
        }
        .build<UUID, Job>()


    override val homeCount get() = homesMap.size

    override fun registerHomes() {
        homesMap.clear()
        HomeConfig.getConfig().homes.forEach { home ->
            homesMap[home.id] = home
        }
    }

    override fun registerHome(home: Home) {
        homesMap[home.id] = home

        HomeConfig.getConfig().apply {
            homes.add(home)
        }
        HomeConfig.save()
    }

    override fun unregisterHome(home: Home) {
        homesMap.remove(home.id)

        HomeConfig.getConfig().apply {
            homes.remove(home)
        }
        HomeConfig.save()
    }

    override fun createHome(ownerId: UUID, name: String, location: Location): HomeCreateResult {
        if (lastCreations.getIfPresent(ownerId) != null) {
            return HomeCreateResult.CooldownActive
        }

        val existingHomes = getHomesOf(ownerId)
        if (existingHomes.size >= MAX_HOMES_PER_PLAYER) {
            return HomeCreateResult.LimitReached
        }

        if (existingHomes.any { it.name.equals(name, ignoreCase = true) }) {
            return HomeCreateResult.NameAlreadyUsed(name)
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
            val minutes = TELEPORT_COOLDOWN.inWholeMinutes
            player.sendText {
                appendErrorPrefix()
                error("Du kannst dich nur alle")
                appendSpace()
                variableValue("$minutes Minuten")
                appendSpace()
                error("teleportieren.")
            }
            return
        }

        player.sendText {
            appendSuccessPrefix()
            success("Du wirst in")
            appendSpace()
            variableValue("$WAIT_TIME Sekunden")
            appendSpace()
            success("zu")
            appendSpace()
            variableValue(home.name)
            appendSpace()
            success("zteleportiert")

            appendNewSuccessPrefixedLine()
            success("Bitte bewege dich nicht!")
        }

        executions.invalidate(uuid)

        val startLocation = withContext(plugin.entityDispatcher(player)) {
            player.location.clone()
        }

        val job = plugin.launch {
            for (secondsLeft in WAIT_TIME.inWholeSeconds downTo 0) {
                if (player.location.distanceSquared(startLocation) > 0.1) {
                    player.sendText {
                        appendErrorPrefix()
                        error("Die Teleportation")
                        appendSpace()
                        variableValue(home.name)
                        appendSpace()
                        error("wurde abgebrochen, da du dich bewegt hast!")
                    }
                    executions.invalidate(uuid)
                    return@launch
                }

                if (secondsLeft > 0) {
                    player.sendRemainingTimeActionBar(home.name, secondsLeft)
                    player.playTeleportSound(false)
                    delay(1.seconds)
                }
            }

            withContext(plugin.entityDispatcher(player)) {
                player.teleportAsync(home.location).thenAccept {
                    teleportCooldowns.put(uuid, Unit)
                    player.playTeleportSound(true)
                    player.sendText {
                        appendSuccessPrefix()
                        success("Du wurdest zu")
                        appendSpace()
                        variableValue(home.name)
                        appendSpace()
                        success("teleportiert.")
                    }
                }
            }
            executions.invalidate(uuid)
        }

        executions.put(uuid, job)
    }

    private fun Player.sendRemainingTimeActionBar(homeName: String, seconds: Long) {
        val content = if (seconds > 0) {
            buildText {
                success("Teleport zu")
                appendSpace()
                variableValue(homeName)
                appendSpace()
                success("in")
                appendSpace()
                variableValue("$seconds Sekunden")
                success("...")
            }
        } else {
            buildText {
                success("Du wirst zu")
                appendSpace()
                variableValue(homeName)
                appendSpace()
                success("teleportiert ...")
            }
        }
        sendActionBar(content)
    }

    private fun Player.playTeleportSound(completed: Boolean) = playSound(true) {
        if (completed) {
            type(BukkitSound.ENTITY_ENDERMAN_TELEPORT)
        } else {
            type(BukkitSound.BLOCK_NOTE_BLOCK_PLING)
        }

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
        HomeConfig.save()
    }
}