package com.pulsewave.app.feature.survey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsewave.app.core.database.dao.PulseWaveDao
import com.pulsewave.app.core.database.entities.SurveyPointEntity
import com.pulsewave.app.core.database.entities.SurveyProjectEntity
import com.pulsewave.app.core.export.ExportManager
import com.pulsewave.app.core.export.csvField
import com.pulsewave.app.core.wifi.WifiNetwork
import com.pulsewave.app.core.wifi.WifiScanner
import com.pulsewave.app.core.wifi.collectFreshLinkSamples
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SurveyViewModel(
    private val wifiScanner: WifiScanner,
    private val dao: PulseWaveDao,
    private val exportManager: ExportManager
) : ViewModel(), KoinComponent {
    private val networkStatus: com.pulsewave.app.core.domain.NetworkStatusRepository by inject()



    val projects = dao.getAllProjects().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    private val _currentProjectId = MutableStateFlow<Long?>(null)
    private val _selectedFloor = MutableStateFlow(1)
    val selectedFloor = _selectedFloor.asStateFlow()
    val currentProject = combine(projects, _currentProjectId) { all, id -> all.find { it.id == id } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    private val projectPoints = _currentProjectId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else dao.getPointsForProject(id)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    /** Floors that already hold readings, plus the one being viewed: what the floor buttons show. */
    val floorsInUse = combine(projectPoints, _selectedFloor) { points, floor ->
        (points.map { it.floorId } + floor + 1).distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, listOf(1))
    val projectPointCount = projectPoints.map { it.size }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val currentPoints = combine(projectPoints, _selectedFloor) { points, floor ->
        points.filter { it.floorId == floor }.sortedWith(compareBy({ it.timestamp }, { it.id }))
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val availableNetworks = wifiScanner.scanResults
    val isScanning = wifiScanner.isScanning
    private val _currentPosition = MutableStateFlow<SurveyPosition?>(null)
    val currentPosition = _currentPosition.asStateFlow()
    private val _isSampling = MutableStateFlow(false)
    val isSampling = _isSampling.asStateFlow()
    /** How long the last successful reading took; the measuring pie fills over this. */
    private val _expectedReadingMillis = MutableStateFlow(LinkReadingMillis)
    val expectedReadingMillis = _expectedReadingMillis.asStateFlow()
    /** True while a reading comes from the live link (known length); false while waiting on a scan. */
    private val _readingIsTimed = MutableStateFlow(true)
    val readingIsTimed = _readingIsTimed.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    private val _isUpdatingProject = MutableStateFlow(false)
    val isUpdatingProject = _isUpdatingProject.asStateFlow()
    private val projectWrites = Mutex()
    private var contextGeneration = 0L
    private val sampleRequests = Channel<SampleRequest>(Channel.CONFLATED)
    private data class SampleRequest(
        val projectId: Long, val floorId: Int, val targetBssid: String,
        val position: SurveyPosition, val generation: Long
    )

    init {
        viewModelScope.launch {
            projects.collect { all ->
                if (all.none { it.id == _currentProjectId.value }) {
                    activateProject(all.firstOrNull()?.id)
                }
            }
        }
        viewModelScope.launch {
            for (request in sampleRequests) {
                if (request.generation != contextGeneration) continue
                _isSampling.value = true
                val startedAt = com.pulsewave.app.core.util.nowMillis()
                try {
                    // Fast path: connected to the mapped access point, read the live link (about 1.5 s).
                    val linkRequestedAt = com.pulsewave.app.core.util.nowMillis()
                    // Only the live link has a known length; a scan for another Wi-Fi does not.
                    // Skip updates where Android hides the access point's address; only a real sample counts.
                    _readingIsTimed.value = kotlinx.coroutines.withTimeoutOrNull(700) {
                        networkStatus.getCurrentNetworkStatus().first { !it.isConnected || it.sampledAtMillis > 0 }
                    }?.let { it.hasSignalMeasurement && it.bssid.equals(request.targetBssid, ignoreCase = true) } ?: true
                    val link = try {
                        kotlinx.coroutines.withTimeoutOrNull(3_000) {
                            collectFreshLinkSamples(networkStatus.getCurrentNetworkStatus(refreshIntervalMs = 500),
                                request.targetBssid, linkRequestedAt, count = 3, minimumIntervalMs = 500)
                        }?.let { linkSurveyMeasurement(it, request.targetBssid, com.pulsewave.app.core.util.nowMillis()) }
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: IllegalStateException) {
                        null // Disconnected or roamed: only a scan of the selected AP can be used.
                    }
                    if (request.generation != contextGeneration) continue
                    if (link != null) _expectedReadingMillis.value = (com.pulsewave.app.core.util.nowMillis() - startedAt).toInt().coerceIn(800, 4_000)
                    else _readingIsTimed.value = false
                    val measurement = link ?: run {
                        // Another access point: only a fresh scan can tell how strong it is here.
                        val requestedAt = com.pulsewave.app.core.util.nowMillis()
                        val scanned = wifiScanner.awaitNextScan()
                        scanned?.let { freshSurveyMeasurement(it, request.targetBssid, requestedAt, com.pulsewave.app.core.util.nowMillis()) }
                            .also { found ->
                                if (found == null) _message.value = if (scanned == null) {
                                    "Android is pausing scans. Stay here and try again in a moment."
                                } else {
                                    "That Wi-Fi didn't show up here. Nothing was saved."
                                }
                            }
                    }
                    if (request.generation != contextGeneration || measurement == null) continue
                    projectWrites.withLock {
                        if (request.generation != contextGeneration) return@withLock
                        // The UI flow may still show the previous target immediately after a write.
                        val persisted = dao.getAllProjects().first().find { it.id == request.projectId }
                        if (persisted?.targetBssid?.equals(request.targetBssid, ignoreCase = true) != true) {
                            _message.value = "The project changed while scanning. No point was saved."
                            return@withLock
                        }
                        val position = request.position
                        dao.insertSurveyPoint(SurveyPointEntity(
                            projectId = request.projectId, floorId = request.floorId,
                            x = position.x, y = position.y, rssi = measurement.rssi,
                            ssid = measurement.ssid, bssid = measurement.bssid, timestamp = measurement.timestamp
                        ))
                        // The new spot on the map is the confirmation; no snackbar over the toolbar.
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    _message.value = "Couldn't save that reading. Your other readings are safe."
                } finally {
                    _isSampling.value = false
                }
            }
        }
    }

    fun dismissMessage() { _message.value = null }
    fun reportMessage(message: String) { _message.value = message }

    /**
     * One tap from "Create a map": named "Home" (then "Home 2"…) and already measuring the
     * Wi-Fi this phone is on, so the first thing people do is tap where they stand.
     */
    fun createMap() {
        viewModelScope.launch {
            val taken = projects.value.map { it.name }.toSet()
            val name = generateSequence(1) { it + 1 }.map { if (it == 1) "Home" else "Home $it" }.first { it !in taken }
            val status = runCatching { networkStatus.getCurrentNetworkStatus().first() }.getOrNull()
            createProject(name, target = status?.takeIf { it.isConnected && it.bssid.isNotBlank() && it.bssid != "02:00:00:00:00:00" }?.let { it.ssid to it.bssid })
        }
    }

    fun createProject(name: String, target: Pair<String, String>? = null) {
        if (name.isBlank()) return
        if (_isUpdatingProject.value) {
            _message.value = "One moment, still saving the map."
            return
        }
        _isUpdatingProject.value = true
        contextGeneration++
        viewModelScope.launch {
            try {
                projectWrites.withLock {
                    val now = com.pulsewave.app.core.util.nowMillis()
                    val id = dao.insertSurveyProject(SurveyProjectEntity(
                        name = name.trim(), description = null, imagePath = null, createdAt = now, updatedAt = now,
                        targetSsid = target?.first, targetBssid = target?.second
                    ))
                    activateProject(id)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _message.value = "Could not create the map. Try again."
            } finally {
                _isUpdatingProject.value = false
            }
        }
    }

    fun renameProject(name: String) {
        val project = currentProject.value ?: return
        if (name.isBlank() || name == project.name) return
        mutateProject(project.id, "Could not rename the map. Try again.") { persisted ->
            dao.updateSurveyProject(persisted.copy(name = name.trim(), updatedAt = com.pulsewave.app.core.util.nowMillis()))
        }
    }

    fun selectProject(id: Long?) {
        activateProject(id)
        val project = projects.value.find { it.id == id } ?: return
        // Persist last-used order without a second settings store; startup reopens this project.
        viewModelScope.launch {
            try {
                projectWrites.withLock {
                    dao.markSurveyProjectUsed(project.id, com.pulsewave.app.core.util.nowMillis())
                }
            }
            catch (_: Exception) { _message.value = "Could not remember the selected project." }
        }
    }

    private fun activateProject(id: Long?) {
        contextGeneration++
        _currentProjectId.value = id
        _selectedFloor.value = 1
        resetPosition()
    }

    fun floorReadingCount(floor: Int): Int = projectPoints.value.count { it.floorId == floor }

    /** Removes every reading on one floor. An emptied floor other than 1 disappears from the buttons. */
    fun deleteFloor(floor: Int) {
        val project = currentProject.value ?: return
        mutateProject(project.id, "Couldn't delete that floor. Try again.") { persisted ->
            dao.deleteFloorPoints(persisted.id, floor)
        }
        if (_selectedFloor.value == floor) setFloor(1)
    }

    fun setFloor(floor: Int) {
        contextGeneration++
        _selectedFloor.value = floor
        resetPosition()
    }

    fun refreshNetworks() {
        viewModelScope.launch {
            try {
                if (wifiScanner.awaitNextScan() == null) {
                    _message.value = "Android did not provide a fresh scan. Try again shortly."
                }
            } catch (_: Exception) {
                _message.value = "Could not scan. Check Wi-Fi and location settings."
            }
        }
    }

    /** A project mutation invalidates samples before its asynchronous database write begins. */
    private fun mutateProject(projectId: Long, errorMessage: String, action: suspend (SurveyProjectEntity) -> Unit) {
        if (_isUpdatingProject.value) {
            _message.value = "One moment, still saving the map."
            return
        }
        _isUpdatingProject.value = true
        contextGeneration++
        viewModelScope.launch {
            try {
                projectWrites.withLock {
                    val persisted = dao.getAllProjects().first().find { it.id == projectId } ?: return@withLock
                    action(persisted)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _message.value = errorMessage
            } finally {
                _isUpdatingProject.value = false
            }
        }
    }

    /** Confirm in the UI before replacing a target; mixed-target maps would be misleading. */
    fun selectTarget(network: WifiNetwork) {
        val project = currentProject.value ?: return
        if (project.targetBssid.equals(network.bssid, ignoreCase = true)) return
        mutateProject(project.id, "Could not switch networks. Try again.") { persisted ->
            dao.replaceSurveyTarget(persisted.copy(
                targetSsid = network.ssid, targetBssid = network.bssid, updatedAt = com.pulsewave.app.core.util.nowMillis()
            ))
            if (_currentProjectId.value == project.id) resetPosition()
        }
    }

    fun setFloorplan(path: String?, onReplaced: (String?) -> Unit = {}) {
        val project = currentProject.value ?: return
        mutateProject(project.id, "Could not save the floorplan.") { persisted ->
            dao.updateSurveyProject(persisted.copy(imagePath = path, updatedAt = com.pulsewave.app.core.util.nowMillis()))
            onReplaced(persisted.imagePath)
        }
    }

    fun addPoint(x: Float, y: Float) {
        if (_isUpdatingProject.value) {
            _message.value = "Wait for the project update before measuring."
            return
        }
        val project = currentProject.value ?: return
        val target = project.targetBssid
        if (target == null) {
            _message.value = "Choose an access point before mapping its coverage."
            return
        }
        if (!x.isFinite() || !y.isFinite()) return
        val position = SurveyPosition(x, y).clamped()
        _currentPosition.value = position
        sampleRequests.trySend(SampleRequest(project.id, _selectedFloor.value, target, position, contextGeneration))
    }

    fun deletePoint(point: com.pulsewave.app.core.database.entities.SurveyPointEntity) {
        mutateProject(point.projectId, "Couldn't remove that reading.") { dao.deleteSurveyPoint(point.id) }
    }

    fun undoLastPoint() {
        val last = currentPoints.value.lastOrNull() ?: return
        mutateProject(last.projectId, "Couldn't remove that reading.") { dao.deleteSurveyPoint(last.id) }
    }

    fun deleteProject(project: SurveyProjectEntity, onDeleted: (String?) -> Unit = {}) {
        mutateProject(project.id, "Could not delete this project.") { persisted ->
            dao.deleteProject(persisted) // Room's foreign key cascades to its points.
            onDeleted(persisted.imagePath)
        }
    }

    /** Leaving the screen cancels a reading in flight, so it can't land on the wrong spot later. */
    fun onScreenStopped() {
        contextGeneration++
    }

    private fun resetPosition() {
        _currentPosition.value = null
    }

    fun exportProject() {
        val projectId = _currentProjectId.value ?: return
        viewModelScope.launch {
            try {
                val snapshot = projectWrites.withLock {
                    val project = dao.getAllProjects().first().find { it.id == projectId } ?: return@withLock null
                    project to dao.getPointsForProject(projectId).first().sortedWith(compareBy({ it.timestamp }, { it.id }))
                } ?: return@launch
                val (project, points) = snapshot
                if (points.isEmpty()) { _message.value = "Save at least one measurement before exporting."; return@launch }
                val csv = mutableListOf("Timestamp,X_Ratio,Y_Ratio,RSSI_dBm,SSID,BSSID,Floor_ID")
                points.forEach { p -> csv += listOf(p.timestamp, p.x, p.y, p.rssi, csvField(p.ssid ?: ""),
                    csvField(p.bssid ?: ""), p.floorId).joinToString(",") }
                val safeName = project.name.replace(Regex("[^A-Za-z0-9._-]+"), "_")
                exportManager.exportToCsv(csv, "Survey_${safeName}_${com.pulsewave.app.core.util.nowMillis()}")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) { _message.value = "Could not export this project. Try again." }
        }
    }

    override fun onCleared() {
        sampleRequests.close()
        super.onCleared()
    }

}
