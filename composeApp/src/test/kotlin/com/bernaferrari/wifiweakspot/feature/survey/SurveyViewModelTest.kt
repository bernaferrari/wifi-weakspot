package com.bernaferrari.wifiweakspot.feature.survey

import androidx.lifecycle.ViewModelStore
import com.bernaferrari.wifiweakspot.core.database.dao.WifiWeakspotDao
import com.bernaferrari.wifiweakspot.core.database.entities.SurveyPointEntity
import com.bernaferrari.wifiweakspot.core.database.entities.SurveyProjectEntity
import com.bernaferrari.wifiweakspot.core.export.ExportManager
import com.bernaferrari.wifiweakspot.core.wifi.WifiNetwork
import com.bernaferrari.wifiweakspot.core.wifi.WifiScanner
import io.mockk.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@OptIn(ExperimentalCoroutinesApi::class)
class SurveyViewModelTest {
    private val store = ViewModelStore()
    private val dao = mockk<WifiWeakspotDao>()
    private val scanner = mockk<WifiScanner>()
    private val exports = mockk<ExportManager>(relaxed = true)
    private val link = mockk<com.bernaferrari.wifiweakspot.core.domain.NetworkStatusRepository>().also {
        every { it.getCurrentNetworkStatus(any()) } returns kotlinx.coroutines.flow.flowOf(com.bernaferrari.wifiweakspot.core.model.NetworkStatus.Disconnected)
    }
    private val projects = MutableStateFlow(listOf(project(1), project(2)))
    private val points = MutableStateFlow<List<SurveyPointEntity>>(emptyList())
    private lateinit var model: SurveyViewModel

    private fun project(id: Long) = SurveyProjectEntity(id, "Project $id", null, null, 1, 1,
        targetSsid = "Home", targetBssid = "selected")
    private fun network(bssid: String = "selected", rssi: Int = -75, timestamp: Long = System.currentTimeMillis()) =
        WifiNetwork("Home", bssid, rssi, 2412, 20, "WPA2", "", timestamp)

    @Before fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        startKoin { modules(module {
            // Not on the mapped access point: readings take the scan path these tests cover.
            single<com.bernaferrari.wifiweakspot.core.domain.NetworkStatusRepository> { link }
        }) }
        every { dao.getAllProjects() } returns projects
        every { dao.getPointsForProject(any()) } answers {
            val projectId = firstArg<Long>()
            points.map { all -> all.filter { it.projectId == projectId } }
        }
        every { scanner.scanResults } returns MutableStateFlow(listOf(network()))
        every { scanner.isScanning } returns MutableStateFlow(false)
        coEvery { scanner.awaitNextScan(any()) } returns listOf(network())
        coEvery { dao.insertSurveyPoint(any()) } coAnswers {
            points.value = points.value + firstArg<SurveyPointEntity>().copy(id = points.value.size.toLong() + 1)
        }
        coEvery { dao.deleteSurveyPoint(any()) } coAnswers {
            val id = firstArg<Long>()
            points.value = points.value.filterNot { it.id == id }
        }
        coEvery { dao.deleteFloorPoints(any(), any()) } coAnswers {
            val projectId = firstArg<Long>(); val floor = secondArg<Int>()
            points.value = points.value.filterNot { it.projectId == projectId && it.floorId == floor }
        }
        coEvery { dao.markSurveyProjectUsed(any(), any()) } coAnswers {
            val id = firstArg<Long>()
            val timestamp = secondArg<Long>()
            projects.value = projects.value.map { if (it.id == id) it.copy(updatedAt = timestamp) else it }.sortedByDescending { it.updatedAt }
        }
        coEvery { dao.updateSurveyProject(any()) } coAnswers {
            val updated = firstArg<SurveyProjectEntity>()
            projects.value = projects.value.map { if (it.id == updated.id) updated else it }.sortedByDescending { it.updatedAt }
        }
        coEvery { dao.replaceSurveyTarget(any()) } coAnswers {
            val updated = firstArg<SurveyProjectEntity>()
            points.value = points.value.filterNot { it.projectId == updated.id }
            projects.value = projects.value.map { if (it.id == updated.id) updated else it }
        }
        model = SurveyViewModel(scanner, dao, exports)
        store.put("survey", model)
    }

    @After fun teardown() { store.clear(); stopKoin(); Dispatchers.resetMain() }

    @Test fun existingProjectsAutomaticallyReopenAndCanBeSwitched() = runTest {
        runCurrent()
        assertEquals(1L, model.currentProject.value?.id)
        model.selectProject(2)
        runCurrent()
        assertEquals(2L, model.currentProject.value?.id)
        model.addPoint(0.75f, 0.25f)
        runCurrent()
        assertEquals(2L, points.value.single().projectId)
        assertEquals(0.75f, points.value.single().x, 0f)
        assertEquals(0.25f, points.value.single().y, 0f)
    }

    @Test fun creatingMapDoesNotLockTargetToAndroidRedactedBssid() = runTest {
        every { link.getCurrentNetworkStatus(any()) } returns kotlinx.coroutines.flow.flowOf(
            com.bernaferrari.wifiweakspot.core.model.NetworkStatus("Your Wi-Fi", "02:00:00:00:00:00",
                5180, 36, -50, 400, "192.168.1.2", true, ""))
        coEvery { dao.insertSurveyProject(any()) } returns 3L
        runCurrent()
        model.createMap()
        runCurrent()
        coVerify(exactly = 1) { dao.insertSurveyProject(match { it.targetBssid == null && it.targetSsid == null }) }
    }

    @Test fun noFreshScanDoesNotSaveCachedResultsOrInventPoorSignal() = runTest {
        coEvery { scanner.awaitNextScan(any()) } returns null
        runCurrent()
        model.addPoint(0.5f, 0.5f)
        runCurrent()
        assertTrue(points.value.isEmpty())
        assertTrue(model.message.value!!.contains("pausing scans"))
        coVerify(exactly = 0) { dao.insertSurveyPoint(any()) }
    }

    @Test fun missingTargetDoesNotSaveStrongNeighbor() = runTest {
        coEvery { scanner.awaitNextScan(any()) } returns listOf(network("neighbor", -20))
        runCurrent()
        model.addPoint(0.5f, 0.5f)
        runCurrent()
        assertTrue(points.value.isEmpty())
        assertTrue(model.message.value!!.contains("didn't show up"))
    }

    @Test fun selectedRadioIsSavedWithItsRealObservationTime() = runTest {
        val observed = System.currentTimeMillis() - 1_000
        coEvery { scanner.awaitNextScan(any()) } returns listOf(network("neighbor", -20), network(timestamp = observed))
        runCurrent()
        model.addPoint(0.5f, 0.5f)
        runCurrent()
        assertEquals("selected", points.value.single().bssid)
        assertEquals(-75, points.value.single().rssi)
        assertEquals(observed, points.value.single().timestamp)
    }

    @Test fun aPendingSampleDoesNotLeakIntoAnotherProject() = runTest {
        val reply = CompletableDeferred<List<WifiNetwork>?>()
        coEvery { scanner.awaitNextScan(any()) } coAnswers { reply.await() }
        runCurrent()
        model.addPoint(0.1f, 0.1f)
        runCurrent()
        model.selectProject(2)
        runCurrent()
        reply.complete(listOf(network()))
        runCurrent()
        assertTrue(points.value.isEmpty())
    }

    @Test fun burstOfRequestsRunsOneScanAtATimeAndKeepsOnlyLatestPendingLocation() = runTest {
        val reply = CompletableDeferred<List<WifiNetwork>?>()
        coEvery { scanner.awaitNextScan(any()) } coAnswers { reply.await() }
        runCurrent()
        model.addPoint(0.1f, 0.1f)
        runCurrent()
        model.addPoint(0.2f, 0.2f)
        model.addPoint(0.9f, 0.9f)
        runCurrent()
        coVerify(exactly = 1) { scanner.awaitNextScan(any()) }
        reply.complete(listOf(network()))
        runCurrent()
        coVerify(exactly = 2) { scanner.awaitNextScan(any()) }
        assertEquals(listOf(0.1f, 0.9f), points.value.map { it.x })
    }

    @Test fun leavingVisibleScreenDiscardsInFlightSample() = runTest {
        val reply = CompletableDeferred<List<WifiNetwork>?>()
        coEvery { scanner.awaitNextScan(any()) } coAnswers { reply.await() }
        runCurrent()
        model.addPoint(0.5f, 0.5f)
        runCurrent()
        model.onScreenStopped()
        reply.complete(listOf(network()))
        runCurrent()
        assertTrue(points.value.isEmpty())
    }

    @Test fun targetChangeAtomicallyClearsEveryFloorAndPersistsSelectedRadio() = runTest {
        points.value = listOf(
            SurveyPointEntity(1, 1, 0.1f, 0.1f, -70, "Old", "selected", 1, 1),
            SurveyPointEntity(2, 1, 0.9f, 0.9f, -80, "Old", "selected", 2, 2)
        )
        runCurrent()
        assertEquals(2, model.projectPointCount.value)
        model.selectTarget(network("replacement"))
        runCurrent()
        assertTrue(points.value.isEmpty())
        assertEquals("replacement", model.currentProject.value?.targetBssid)
        coVerify(exactly = 1) { dao.replaceSurveyTarget(match { it.targetBssid == "replacement" }) }
    }

    @Test fun undoRemovesLatestPointOnSelectedFloorOnly() = runTest {
        points.value = listOf(
            SurveyPointEntity(1, 1, 0.1f, 0.1f, -70, "Home", "selected", 1, 1),
            SurveyPointEntity(2, 1, 0.9f, 0.9f, -80, "Home", "selected", 2, 2)
        )
        runCurrent()
        model.undoLastPoint()
        runCurrent()
        assertEquals(listOf(2L), points.value.map { it.id })
    }

    @Test fun lastSelectedProjectReopensAfterViewModelRecreation() = runTest {
        runCurrent()
        model.selectProject(2)
        runCurrent()
        store.clear()
        model = SurveyViewModel(scanner, dao, exports)
        store.put("reopened", model)
        runCurrent()
        assertEquals(2L, model.currentProject.value?.id)
    }

    @Test fun retargetingDoesNotQueueSamplesForOldRadioDuringPendingTransaction() = runTest {
        val transaction = CompletableDeferred<Unit>()
        coEvery { dao.replaceSurveyTarget(any()) } coAnswers {
            transaction.await()
            val updated = firstArg<SurveyProjectEntity>()
            points.value = emptyList()
            projects.value = projects.value.map { if (it.id == updated.id) updated else it }
        }
        runCurrent()
        model.selectTarget(network("replacement"))
        runCurrent()
        assertEquals("selected", model.currentProject.value?.targetBssid)
        model.addPoint(0.4f, 0.4f)
        runCurrent()
        coVerify(exactly = 0) { scanner.awaitNextScan(any()) }
        transaction.complete(Unit)
        runCurrent()
        assertTrue(points.value.isEmpty())
        assertEquals("replacement", model.currentProject.value?.targetBssid)
    }

    @Test fun persistedTargetIsCheckedAgainBeforeInsertingMeasurement() = runTest {
        runCurrent()
        coEvery { scanner.awaitNextScan(any()) } coAnswers {
            projects.value = projects.value.map { it.copy(targetBssid = "replacement") }
            listOf(network("selected"))
        }
        model.addPoint(0.4f, 0.4f)
        runCurrent()
        assertTrue(points.value.isEmpty())
        coVerify(exactly = 0) { dao.insertSurveyPoint(any()) }
    }

    @Test fun markingProjectUsedCannotOverwriteNewerTargetMetadata() = runTest {
        val marker = CompletableDeferred<Unit>()
        coEvery { dao.markSurveyProjectUsed(any(), any()) } coAnswers {
            val id = firstArg<Long>()
            val timestamp = secondArg<Long>()
            marker.await()
            projects.value = projects.value.map { if (it.id == id) it.copy(updatedAt = timestamp) else it }
        }
        runCurrent()
        model.selectProject(1)
        runCurrent()
        projects.value = projects.value.map { if (it.id == 1L) it.copy(targetBssid = "replacement", imagePath = "/new.png") else it }
        marker.complete(Unit)
        runCurrent()
        assertEquals("replacement", model.currentProject.value?.targetBssid)
        assertEquals("/new.png", model.currentProject.value?.imagePath)
        coVerify(exactly = 0) { dao.updateSurveyProject(any()) }
    }

    @Test fun deletingAFloorKeepsTheOtherFloorsAndReturnsToFloorOne() = runTest {
        runCurrent()
        points.value = listOf(
            SurveyPointEntity(1, 1, 0.2f, 0.2f, -50, "Home", "selected", floorId = 1, timestamp = 1),
            SurveyPointEntity(2, 1, 0.4f, 0.4f, -60, "Home", "selected", floorId = 2, timestamp = 2)
        )
        model.setFloor(2)
        runCurrent()
        assertEquals(1, model.floorReadingCount(2))
        model.deleteFloor(2)
        runCurrent()
        assertEquals(listOf(1), points.value.map { it.floorId })
        assertEquals(1, model.selectedFloor.value)
    }
}
