package com.pulsewave.app.core.lan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MdnsTest {
    /** Builds a response the way a real responder does, with name compression. */
    private fun response(): ByteArray {
        val out = ArrayList<Byte>()
        fun u8(v: Int) { out += v.toByte() }
        fun u16(v: Int) { u8(v shr 8); u8(v and 0xFF) }
        fun labels(vararg parts: String) = parts.forEach { p -> val b = p.encodeToByteArray(); u8(b.size); b.forEach { out += it } }
        u16(0); u16(0x8400); u16(0); u16(1); u16(0); u16(3)
        // PTR _airplay._tcp.local -> Sala de Estar._airplay._tcp.local
        val typeAt = out.size
        labels("_airplay", "_tcp", "local"); u8(0)
        u16(12); u16(1); u16(0); u16(120)
        val rdata = ArrayList<Byte>()
        u16(0) // placeholder length, patched below
        val lenAt = out.size - 2
        val instanceAt = out.size
        labels("Sala de Estar"); u8(0xC0); u8(typeAt)
        val len = out.size - instanceAt
        out[lenAt] = (len shr 8).toByte(); out[lenAt + 1] = (len and 0xFF).toByte()
        // SRV instance -> sala.local
        u8(0xC0); u8(instanceAt); u16(33); u16(0x8001); u16(0); u16(120)
        val hostAt = out.size + 2 + 6
        u16(6 + 12); u16(0); u16(0); u16(7000); labels("sala", "local"); u8(0)
        // TXT model
        u8(0xC0); u8(instanceAt); u16(16); u16(0x8001); u16(0); u16(120)
        val txt = "model=AppleTV14,1".encodeToByteArray()
        u16(txt.size + 1); u8(txt.size); txt.forEach { out += it }
        // A sala.local -> 192.168.0.130
        u8(0xC0); u8(hostAt); u16(1); u16(0x8001); u16(0); u16(120); u16(4); u8(192); u8(168); u8(0); u8(130)
        rdata.clear()
        return out.toByteArray()
    }

    @Test fun parsesAnAirPlayAnnouncement() {
        val announcement = Mdns.parse(response()).announcements().single()
        assertEquals("192.168.0.130", announcement.ip)
        assertEquals("Sala de Estar", announcement.name)
        assertEquals("AppleTV14,1", announcement.model)
        assertEquals(setOf("_airplay._tcp"), announcement.services)
    }

    @Test fun queryAsksForEveryType() {
        val query = Mdns.query(listOf("_airplay._tcp", "_hap._tcp"))
        assertEquals(2, ((query[4].toInt() and 0xFF) shl 8) or (query[5].toInt() and 0xFF))
    }

    @Test fun cleansMachineNames() {
        assertEquals("Sala de Estar", cleanInstanceName("1A290FAD7E73@Sala de Estar", "_raop._tcp"))
        assertEquals("S90D", cleanInstanceName("S90D-347fc43625d35d8379e0e726f02fb84f", "_googlecast._tcp"))
        assertNull(cleanInstanceName("7a4fa1e6c731778f3d925f14fd0c190e787a18ac", "_spotify-connect._tcp"))
        assertEquals("YLBulbColor1s", cleanInstanceName("YLBulbColor1s-7164", "_hap._tcp"))
        assertEquals("Yeelink light strip", cleanInstanceName("yeelink-light-strip1_miio72698638", "_miio._udp"))
    }
}
