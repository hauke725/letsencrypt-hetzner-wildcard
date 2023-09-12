import dto.external.HetznerRecord
import dto.external.HetznerRecords
import dto.external.HetznerSetRecord
import dto.external.HetznerZones
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.curl.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.utils.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

class Hetzner (private val apiToken: String) {

    private val client: HttpClient by lazy()
    {
        HttpClient(Curl) {
            defaultRequest {
                url("https://dns.hetzner.com/api/v1/")
                header("Auth-API-Token", apiToken)
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    fun createOrUpdate(name: String, value: String) {
        val regex = Regex("(_acme-challenge(\\.\\w+))*\\.(\\w+\\.\\w+)\\.")
        println("matching $name")
        val matches = regex.find(name)?.groupValues
        if (matches.isNullOrEmpty()) {
            throw RuntimeException("domain $name does not match pattern")
        }
        print(matches.joinToString("\n"))
        val domain = matches.last()
        val recordName = matches[1]
        val zoneId = getZoneId(domain)
        createOrUpdateInZone(zoneId, recordName, value)
    }

    fun getZoneId(domain: String): String {
        val zones: HetznerZones = runBlocking {
            client.get("zones").body()
        }
        for (zone in zones.zones) {
            if (zone.name == domain) {
                return zone.id
            }
        }
        throw RuntimeException("zone for domain $domain not found")
    }

    fun createOrUpdateInZone(zoneId: String, name: String, value: String) {
        val record = getRecord(zoneId, name)
        if (record != null) {
            println("found record $name, updating with $value")
            updateRecord(zoneId, record, value)
        } else {
            println("record $name not found, creating with $value")
            createRecord(zoneId, name, value)
        }
    }

    fun getRecord(zoneId: String, name: String): HetznerRecord? {
        for (record in getRecords(zoneId)) {
            if (record.name == name) {
                return record
            }
        }
        return null
    }

    fun createRecord(zoneId: String, name: String, value: String) {
        runBlocking {
            client.post("records") {
                contentType(ContentType.Application.Json)
                setBody(HetznerSetRecord(zoneId, value, name))
            }
        }
    }

    fun updateRecord(zoneId: String, record: HetznerRecord, value: String) {
        val response = runBlocking {
            client.put("records/${record.id}") {
                contentType(ContentType.Application.Json)
                setBody(HetznerSetRecord(zoneId, value, record.name))
            }
        }
        if (response.status.value >= 300) {
            // something went wrong
            val body: String = runBlocking {
                response.body()
            }
            throw RuntimeException("failed to update record: $body")
        }
    }

    fun getRecords(zoneId: String): List<HetznerRecord> {
        val records: HetznerRecords = runBlocking {
            client.get("records?zone_id=$zoneId").body()
        }
        return records.records
    }

}