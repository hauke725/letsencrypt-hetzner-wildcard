import platform.posix.*
import kotlin.native.OsFamily.*
import kotlinx.cinterop.*

class LetsEncrypt(private val host: String) {
    fun requestCert () {
        val hetznerToken = getenv("HETZNER_TOKEN")?.toKString()

        if (hetznerToken.isNullOrEmpty()) {
            throw RuntimeException("env HETZNER_TOKEN is required")
        }
        val hetzner = Hetzner(hetznerToken)

        val platform  = Platform
        val os = platform.osFamily
        println("OS detected as $os")
        val result = when (os) {
            LINUX -> runLinux(hetzner)
            else -> throw RuntimeException("os $os currently not supported")
        }
        println("result:")
        println(result)
    }

    fun runLinux(hetzner: Hetzner): String {
        val command = "certbot certonly --manual -d  $host --preferred-challenges dns"
        val fp = popen(command, "r") ?: error("Failed to run command: $command")

        val stdout = buildString {
            val buffer = ByteArray(4096)
            var next = 0
            var name: String? = null
            var value: String? = null
            while (true) {
                val input = fgets(buffer.refTo(0), buffer.size, fp) ?: break
                val line = input.toKString()
                if (next > 0) {
                    if (line.trim().isEmpty()) {
                        continue
                    }
                    next--
                    when (next) {
                        2 -> {
                            name = line.trim()
                        }
                        0 -> {
                            value = line.trim()
                        }
                        else -> continue
                    }
                    if (name != null && value != null) {
                        println("we have everything, trying to create")
                        hetzner.createOrUpdate(name, value)
                        fputs("\n", fp)
                    }
                } else {
                    if (line.contains("You have an existing certificate that has exactly the same domains or certificate name you requested and")) {
                        println("certificate already there and not close to expiry")
                        fputs("1", fp)
                        break
                    }
                    next = if (line.contains("DNS TXT")) 3 else 0
                }
            }
        }

        val status = pclose(fp)
        if (status != 0) {
            error("Command `$command` failed with status $status")
        }

        return stdout.trim()
    }
}