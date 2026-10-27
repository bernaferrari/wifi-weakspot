package com.bernaferrari.wifiweakspot.core.wifi

import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.SystemClock
import androidx.core.content.ContextCompat
import com.bernaferrari.wifiweakspot.core.root.RootAccess
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Test

class AndroidWifiScannerTest {
    @After fun cleanup() = unmockkAll()

    @Test fun countsAcceptedRequestsFromBothEntryPointsButNotRejections() = runTest {
        mockkStatic(ContextCompat::class)
        mockkStatic(SystemClock::class)
        every { ContextCompat.checkSelfPermission(any(), any()) } returns PackageManager.PERMISSION_GRANTED
        every { SystemClock.elapsedRealtimeNanos() } returns 1_000_000L
        val wifi = mockk<WifiManager>(relaxed = true)
        val context = mockk<Context>(relaxed = true)
        every { context.applicationContext } returns context
        every { context.getSystemService(Context.WIFI_SERVICE) } returns wifi
        val root = mockk<RootAccess>()
        every { root.requestScan() } returns false
        every { wifi.startScan() } returnsMany listOf(true, false, true)
        var accepted = 0
        val scanner = AndroidWifiScanner(context, root) { accepted++ }

        assertTrue(scanner.startScan())
        assertEquals(1, accepted)
        assertFalse(scanner.startScan()) // Already in flight.
        assertEquals(1, accepted)
        scanner.stopScan()
        assertNull(scanner.awaitNextScan(1)) // Platform rejects this request.
        assertEquals(1, accepted)
        assertNull(scanner.awaitNextScan(1)) // Accepted, but no fresh broadcast arrives.
        assertEquals(2, accepted)
    }
}
