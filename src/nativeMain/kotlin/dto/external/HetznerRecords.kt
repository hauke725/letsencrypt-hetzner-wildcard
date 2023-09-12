package dto.external

import kotlinx.serialization.*

@Serializable
data class HetznerRecords(val records: List<HetznerRecord>)

@Serializable
data class HetznerRecord(val name: String, val id: String, val value: String)

@Serializable
data class HetznerSetRecord(val zone_id: String, val value: String, val name: String, @Required val type: String = "TXT", val ttl: Int = 0)