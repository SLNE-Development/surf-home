package dev.slne.surf.home.home

import dev.slne.surf.surfapi.core.api.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectList
import org.bukkit.Location
import org.bukkit.entity.Player
import org.jetbrains.annotations.Unmodifiable
import java.util.*

private val homeService = requiredService<HomeService>()

interface HomeService {
    val homeCount: Int
    val homes: @Unmodifiable ObjectList<Home>

    fun registerHomes()
    fun registerHome(home: Home)
    fun unregisterHome(home: Home)

    fun createHome(ownerId: UUID, name: String, location: Location): HomeCreateResult
    fun deleteHome(homeId: UUID)
    suspend fun teleportPlayerToHome(player: Player, home: Home)

    fun getHomeById(id: UUID): Home?
    fun getHomesOf(ownerId: UUID): List<Home>
    fun getHomeByName(ownerId: UUID, name: String): Home?

    fun generateUnusedId(): UUID

    fun saveHomes()

    companion object : HomeService by homeService
}