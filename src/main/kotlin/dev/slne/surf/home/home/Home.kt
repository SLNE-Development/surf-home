package dev.slne.surf.home.home

import dev.slne.surf.surfapi.bukkit.api.extensions.server
import kotlinx.serialization.Transient
import org.bukkit.Location
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting
import java.time.OffsetDateTime
import java.util.*

fun home(
    id: UUID,
    name: String,
    ownerId: UUID,
    location: Location
) = Home(
    id = id,
    name = name,
    ownerUuid = ownerId,
    worldId = location.world.uid,
    x = location.x,
    y = location.y,
    z = location.z,
    yaw = location.yaw,
    pitch = location.pitch,
    createdAt = OffsetDateTime.now()
)

@ConfigSerializable
data class Home(
    val id: UUID,
    var name: String,
    val ownerUuid: UUID,

    @Setting("world")
    var worldId: UUID,
    var x: Double,
    var y: Double,
    var z: Double,
    var yaw: Float,
    var pitch: Float,

    val createdAt: OffsetDateTime
) {
    @Transient
    var location: Location
        get() = Location(
            server.getWorld(worldId) ?: error("World with id $worldId not found!"),
            x, y, z,
            yaw, pitch
        )
        set(value) {
            worldId = value.world.uid
            x = value.x
            y = value.y
            z = value.z
            yaw = value.yaw
            pitch = value.pitch
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Home
        return id == other.id
    }

    override fun hashCode() = id.hashCode()

    override fun toString(): String {
        return "Home(id=$id, name=$name, ownerId=$ownerUuid, location=$location)"
    }
}
