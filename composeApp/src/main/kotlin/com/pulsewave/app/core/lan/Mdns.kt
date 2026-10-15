package com.pulsewave.app.core.lan

/**
 * Just enough multicast DNS (RFC 6762/6763) to learn what devices call themselves:
 * build a PTR query for a few service types, then read PTR, SRV, TXT and A records
 * from whatever answers. No sockets here, so it runs the same everywhere and in tests.
 */
object Mdns {
    const val Port = 5353
    const val Group = "224.0.0.251"

    /** Services that reveal a friendly name and, often, what kind of thing is answering. */
    val ServiceTypes = listOf(
        "_googlecast._tcp", "_airplay._tcp", "_raop._tcp", "_companion-link._tcp", "_device-info._tcp",
        "_hap._tcp", "_spotify-connect._tcp", "_sonos._tcp", "_amzn-wplay._tcp", "_androidtvremote2._tcp",
        "_ipp._tcp", "_printer._tcp", "_pdl-datastream._tcp", "_scanner._tcp",
        "_smb._tcp", "_workstation._tcp", "_ssh._tcp", "_rfb._tcp", "_nvstream._tcp", "_http._tcp",
        "_miio._udp", "_matter._tcp", "_meshcop._udp"
    )

    private const val TypeA = 1
    private const val TypePtr = 12
    private const val TypeTxt = 16
    private const val TypeSrv = 33

    /** One query asking for every service type at once. [unicast] asks responders to reply directly. */
    fun query(types: List<String> = ServiceTypes, unicast: Boolean = false): ByteArray {
        val out = ArrayList<Byte>()
        fun u16(v: Int) { out += (v shr 8 and 0xFF).toByte(); out += (v and 0xFF).toByte() }
        u16(0); u16(0); u16(types.size); u16(0); u16(0); u16(0)
        types.forEach { type ->
            "$type.local".split('.').forEach { label ->
                val bytes = label.encodeToByteArray()
                out += bytes.size.toByte()
                bytes.forEach { out += it }
            }
            out += 0
            u16(TypePtr)
            u16(if (unicast) 0x8001 else 0x0001)
        }
        return out.toByteArray()
    }

    /** Records pulled from one response packet. Names are lower-cased for matching. */
    class Records {
        /** service type ("_airplay._tcp.local") to instance names. */
        val pointers = mutableListOf<Pair<String, String>>()
        /** instance to target host. */
        val services = mutableMapOf<String, String>()
        /** instance to TXT key/values. */
        val texts = mutableMapOf<String, Map<String, String>>()
        /** host to IPv4 address. */
        val addresses = mutableMapOf<String, String>()

        fun addAll(other: Records) {
            pointers += other.pointers
            services += other.services
            texts += other.texts
            addresses += other.addresses
        }
    }

    /** Parses answers and additional records; malformed packets yield whatever was read before the fault. */
    fun parse(packet: ByteArray): Records {
        val records = Records()
        runCatching {
            val reader = Reader(packet)
            reader.pos = 4
            val questions = reader.u16()
            val total = reader.u16() + reader.u16() + reader.u16()
            repeat(questions) { reader.name(); reader.pos += 4 }
            repeat(total) {
                val owner = reader.name()
                val type = reader.u16()
                reader.pos += 2 + 4
                val length = reader.u16()
                val end = reader.pos + length
                when (type) {
                    TypePtr -> records.pointers += owner.lowercase() to reader.name()
                    TypeSrv -> { reader.pos += 6; records.services[owner.lowercase()] = reader.name().lowercase() }
                    TypeTxt -> records.texts[owner.lowercase()] = reader.txt(end)
                    TypeA -> if (length == 4) {
                        records.addresses[owner.lowercase()] = (0 until 4).joinToString(".") { (packet[reader.pos + it].toInt() and 0xFF).toString() }
                    }
                }
                reader.pos = end
            }
        }
        return records
    }

    private class Reader(val data: ByteArray) {
        var pos = 0
        fun u8(at: Int = pos) = data[at].toInt() and 0xFF
        fun u16(): Int = ((u8(pos) shl 8) or u8(pos + 1)).also { pos += 2 }

        /** Reads a possibly compressed name, keeping case so instance names stay readable. */
        fun name(): String {
            val labels = mutableListOf<String>()
            var at = pos
            var jumped = false
            var hops = 0
            while (true) {
                val len = u8(at)
                when {
                    len == 0 -> { if (!jumped) pos = at + 1; break }
                    len and 0xC0 == 0xC0 -> {
                        val target = ((len and 0x3F) shl 8) or u8(at + 1)
                        if (!jumped) pos = at + 2
                        jumped = true
                        at = target
                        if (++hops > 32) error("Name loop")
                    }
                    else -> {
                        labels += data.decodeToString(at + 1, at + 1 + len)
                        at += 1 + len
                    }
                }
            }
            return labels.joinToString(".")
        }

        fun txt(end: Int): Map<String, String> {
            val values = mutableMapOf<String, String>()
            while (pos < end) {
                val len = u8(pos)
                val entry = data.decodeToString(pos + 1, minOf(end, pos + 1 + len))
                pos += 1 + len
                val key = entry.substringBefore('=').lowercase()
                if (key.isNotEmpty()) values[key] = entry.substringAfter('=', "")
            }
            return values
        }
    }
}

/** What a device said about itself over mDNS, gathered across all its services. */
data class Announcement(
    val ip: String,
    /** Best human name, such as "Sala de Estar" or "Bernardo's MacBook Pro". */
    val name: String?,
    /** Model hint from TXT records, such as "AppleTV14,1" or "Chromecast". */
    val model: String?,
    /** Service types without the ".local" suffix, such as "_airplay._tcp". */
    val services: Set<String>
)

/** Name sources, best first: a Cast "fn" is set by the owner; AirPlay names come next. */
private val NamePriority = listOf(
    "_googlecast._tcp", "_airplay._tcp", "_companion-link._tcp", "_device-info._tcp", "_raop._tcp",
    "_sonos._tcp", "_amzn-wplay._tcp", "_hap._tcp", "_ipp._tcp", "_printer._tcp", "_smb._tcp", "_workstation._tcp",
    "_nvstream._tcp", "_rfb._tcp", "_ssh._tcp", "_androidtvremote2._tcp", "_matter._tcp", "_miio._udp", "_http._tcp", "_spotify-connect._tcp"
)

/** Turns raw records into one announcement per IPv4 address. */
fun Mdns.Records.announcements(): List<Announcement> {
    data class Seen(val type: String, val instance: String, val label: String)
    val byIp = mutableMapOf<String, MutableList<Seen>>()
    pointers.forEach { (typeName, instance) ->
        val type = typeName.removeSuffix(".local")
        if (!type.startsWith("_")) return@forEach
        val key = instance.lowercase()
        val ip = services[key]?.let { addresses[it] } ?: return@forEach
        // The instance is everything before the service type: "Sala de Estar._airplay._tcp.local".
        val label = instance.removeSuffix(".local").removeSuffix(".$type").ifBlank { return@forEach }
        byIp.getOrPut(ip) { mutableListOf() } += Seen(type, key, label)
    }
    return byIp.map { (ip, seen) ->
        val txt = seen.flatMap { texts[it.instance].orEmpty().entries }.associate { it.key to it.value }
        val castName = txt["fn"]?.takeIf { seen.any { it.type == "_googlecast._tcp" } }
        val name = castName ?: seen.sortedBy { s -> NamePriority.indexOf(s.type).let { if (it < 0) 99 else it } }
            .map { cleanInstanceName(it.label, it.type) }
            .firstOrNull { it != null }
        val model = txt["md"] ?: txt["model"] ?: txt["am"] ?: txt["ty"] ?: txt["usb_mdl"]
        Announcement(ip, name, model, seen.map { it.type }.toSet())
    }
}

/** Strips machine prefixes: "1A290FAD7E73@Sala de Estar" → "Sala de Estar"; hex IDs and serials → null. */
internal fun cleanInstanceName(label: String, type: String): String? {
    var name = label.substringAfter('@').replace("\\032", " ").trim()
    // Cast and Spotify instances are often "Model-<hex id>"; keep the readable model part.
    if ((type == "_googlecast._tcp" || type == "_spotify-connect._tcp") && '-' in name) name = name.substringBefore('-')
    // Xiaomi: "yeelink-light-strip1_miio72698638" → "Yeelink light strip".
    if (type == "_miio._udp") {
        name = name.substringBefore("_miio").replace('-', ' ').trimEnd { it.isDigit() }.trim().replaceFirstChar { it.uppercase() }
    }
    // HomeKit accessories append a short hex suffix: "YLBulbColor1s-7164" → "YLBulbColor1s".
    if (type == "_hap._tcp") name = name.replace(Regex("""-[0-9A-Fa-f]{4}$"""), "")
    if (name.length < 2) return null
    // Serials and device IDs are long runs of hex, sometimes with dashes or colons.
    val looksLikeId = name.length >= 12 && name.all { it in "0123456789abcdefABCDEF-:" }
    return name.takeUnless { looksLikeId }
}
