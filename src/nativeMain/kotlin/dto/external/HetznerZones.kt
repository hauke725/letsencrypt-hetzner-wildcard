package dto.external

import kotlinx.serialization.Serializable

@Serializable
data class HetznerZones(val zones: List<HetznerZone>)

@Serializable
data class HetznerZone(val id: String, val name: String)


