package com.pulsewave.app.core.lan

import com.pulsewave.app.core.model.lan.LanDevice
import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceIdentityTest {
    private fun device(ip: String, host: String? = null, vendor: String? = null, custom: String? = null) =
        LanDevice(ip, null, host, vendor, 0L, true, false, custom, emptyList())

    private fun id(d: LanDevice) = d.identify(gateway = "192.168.1.1", phoneIp = "192.168.1.23")

    @Test fun gatewayIsTheRouter() {
        val identity = id(device("192.168.1.1", host = "router.lan", vendor = "TP-Link"))
        assertEquals("Your router", identity.name)
        assertEquals(DeviceKind.Router, identity.kind)
        assertEquals("TP-Link · 192.168.1.1", identity.detail)
    }

    @Test fun phoneIpIsThisPhone() = assertEquals(DeviceKind.ThisPhone, id(device("192.168.1.23", host = "pixel-8")).kind)

    @Test fun hostnameBecomesAFriendlyName() {
        val identity = id(device("192.168.1.31", host = "living-room-tv.lan", vendor = "Samsung Electronics Co.,Ltd"))
        assertEquals("Living room TV", identity.name)
        assertEquals(DeviceKind.Tv, identity.kind)
        assertEquals("Samsung · 192.168.1.31", identity.detail)
    }

    @Test fun unknownChipMakerReadsAsSmartHome() {
        val identity = id(device("192.168.1.71", vendor = "Espressif Inc."))
        assertEquals("Espressif smart home device", identity.name)
        assertEquals(DeviceKind.SmartHome, identity.kind)
    }

    @Test fun brandsKeepTheirCasing() = assertEquals("Ana's iPhone", friendlyHostname("Ana's-iphone.local"))

    private fun announced(ip: String, name: String, model: String? = null, vararg services: String) =
        LanDevice(ip, null, null, null, 0L, true, false, null, emptyList(), announcedName = name, model = model, services = services.toList())

    @Test fun appleTvByModel() {
        val identity = id(announced("192.168.1.80", "Sala de Estar", "AppleTV14,1", "_airplay._tcp", "_companion-link._tcp"))
        assertEquals("Sala de Estar", identity.name)
        assertEquals(DeviceKind.Tv, identity.kind)
    }

    @Test fun airPlayOnlyIsATv() = assertEquals(DeviceKind.Tv, id(announced("192.168.1.81", "55\" OLED", null, "_airplay._tcp", "_raop._tcp")).kind)

    @Test fun macByName() = assertEquals(DeviceKind.Computer, id(announced("192.168.1.82", "Ferrari’s Mac mini", null, "_airplay._tcp", "_companion-link._tcp")).kind)

    @Test fun homeKitIsSmartHome() = assertEquals(DeviceKind.SmartHome, id(announced("192.168.1.83", "YLBulbColor1s-7164", null, "_hap._tcp")).kind)

    @Test fun thisPhoneShowsItsName() =
        assertEquals("Galaxy S25 · 192.168.1.23", id(announced("192.168.1.23", "Galaxy S25")).detail)

    @Test fun nothingKnownIsUnnamed() = assertEquals("Unnamed device", id(device("192.168.1.90")).name)

    @Test fun customNameWins() = assertEquals("Grandma's iPad", id(device("192.168.1.40", host = "ipad", custom = "Grandma's iPad")).name)

    @Test fun shortHintsDoNotMatchInsideWords() = assertEquals(DeviceKind.Unknown, id(device("192.168.1.50", host = "epcot")).kind)
}
