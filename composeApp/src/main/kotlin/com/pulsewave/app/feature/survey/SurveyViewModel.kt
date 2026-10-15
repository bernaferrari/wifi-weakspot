package com.pulsewave.app.feature.survey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsewave.app.core.database.dao.PulseWaveDao
import com.pulsewave.app.core.database.entities.SurveyPointEntity
import com.pulsewave.app.core.database.entities.SurveyProjectEntity
import com.pulsewave.app.core.export.ExportManager
import com.pulsewave.app.core.export.csvField
import com.pulsewave.app.core.sensors.OrientationProvider
import com.pulsewave.app.core.sensors.StepCounter
import com.pulsewave.app.core.wifi.WifiNetwork
import com.pulsewave.app.core.wifi.WifiScanner
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
    private val stepCounter: StepCounter by inject()
    private val orientationProvider: OrientationProvider by inject()
    private val networkStatus: com.pulsewave.app.core.domain.NetworkStatusRepository by inject()

    enum class SurveyMode { AUTO_SAMPLE, MANUAL_ANCHOR }

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
    private val _surveyMode = MutableStateFlow(SurveyMode.MANUAL_ANCHOR)
    val surveyMode = _surveyMode.asStateFlow()
    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()
    private val _currentHeading = MutableStateFlow(0f)
    val currentHeading = _currentHeading.asStateFlow()
    private val _currentPosition = MutableStateFlow<SurveyPosition?>(null)
    val currentPosition = _currentPosition.asStateFlow()
    private val _ghostPath = MutableStateFlow<List<Pair<Float, Float>>>(emptyList())
    val ghostPath = _ghostPath.asStateFlow()
    private val _isSampling = MutableStateFlow(false)
    val isSampling = _isSampling.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    private val _isUpdatingProject = MutableStateFlow(false)
    val isUpdatingProject = _isUpdatingProject.asStateFlow()
    private val projectWrites = Mutex()
    private var sensorJob: Job? = null
    private var contextGeneration = 0L
    private var lastAutoSampleAt = 0L
    private val sampleRequests = Channel<SampleRequest>(Channel.CONFLATED)
    private data class SampleRequest(
        val projectId: Long, val floorId: Int, val targetBssid: String,
        val position: SurveyPosition, val generation: Long, val automatic: Boolean
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
                try {
                    val requestedAt = com.pulsewave.app.core.util.nowMillis()
                    val scanned = wifiScanner.awaitNextScan()
                    if (request.generation != contextGeneration) continue
                    val measurement = scanned?.let {
                        freshSurveyMeasurement(it, request.targetBssid, requestedAt, com.pulsewave.app.core.util.nowMillis())
                    }
                    if (measurement == null) {
                        _message.value = if (scanned == null) {
                            "No fresh scan available. Stay at this location and try again after Android allows another scan."
                        } else {
                            "The selected access point was not observed in this scan. No point was saved."
                        }
                        continue
                    }
                    projectWrites.withLock {
                        if (request.generation != contextGeneration) return@withLock
                        // The UI flow may still show the previous target immediately after a write.
                        val persisted = dao.getAllProjects().first().find { it.id == request.projectId }
                        if (persisted?.targetBssid?.equals(request.targetBssid, ignoreCase = true) != true) {
                            _message.value = "The project changed while scanning. No point was saved."
                            return@withLock
                        }
                        val position = if (request.automatic) _currentPosition.value ?: request.position else request.position
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
            createProject(name, target = status?.takeIf { it.isConnected && it.bssid.isNotBlank() }?.let { it.ssid to it.bssid })
        }
    }

    fun createProject(name: String, target: Pair<String, String>? = null) {
        if (name.isBlank()) return
        if (_isUpdatingProject.value) {
            _message.value = "One moment, still saving the map."
            return
        }
        _isUpdatingProject.value = true
        stopRecording()
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
        stopRecording()
        contextGeneration++
        _currentProjectId.value = id
        _selectedFloor.value = 1
        resetPosition()
    }

    fun setFloor(floor: Int) {
        stopRecording()
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
        stopRecording()
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

    fun configureMap(widthMeters: Float, heightMeters: Float, stepLengthMeters: Float, mapTopBearingDegrees: Float) {
        val project = currentProject.value ?: return
        if (!widthMeters.isFinite() || !heightMeters.isFinite() || !stepLengthMeters.isFinite() ||
            widthMeters !in 1f..500f || heightMeters !in 1f..500f || stepLengthMeters !in 0.2f..1.5f ||
            !mapTopBearingDegrees.isFinite() || mapTopBearingDegrees !in 0f..359.99f) return
        mutateProject(project.id, "Could not save map settings.") { persisted ->
            dao.updateSurveyProject(persisted.copy(
                widthMeters = widthMeters, heightMeters = heightMeters, stepLengthMeters = stepLengthMeters,
                mapTopBearingDegrees = mapTopBearingDegrees, updatedAt = com.pulsewave.app.core.util.nowMillis()
            ))
        }
    }

    fun setFloorplan(path: String?, onReplaced: (String?) -> Unit = {}) {
        val project = currentProject.value ?: return
        mutateProject(project.id, "Could not save the floorplan.") { persisted ->
            dao.updateSurveyProject(persisted.copy(imagePath = path, updatedAt = com.pulsewave.app.core.util.nowMillis()))
            onReplaced(persisted.imagePath)
        }
    }

    fun addPoint(x: Float, y: Float, automatic: Boolean = false) {
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
        sampleRequests.trySend(SampleRequest(project.id, _selectedFloor.value, target, position, contextGeneration, automatic))
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

    /** Read live by the walking loop, so it can change mid-walk. */
    fun setSurveyMode(mode: SurveyMode) {
        _surveyMode.value = mode
    }

    fun toggleRecording() {
        if (_isUpdatingProject.value) {
            _message.value = "Wait for the project update before starting a walk."
            return
        }
        if (_isRecording.value) { stopRecording(); return }
        if (currentProject.value?.targetBssid == null) {
            _message.value = "Choose an access point before starting a walk."
            return
        }
        if (_currentPosition.value == null) {
            _message.value = "Set your starting position by tapping the map or entering coordinates."
            return
        }
        if (!stepCounter.isAvailable || !orientationProvider.isAvailable) {
            _message.value = "This device cannot estimate a walking path. Use map taps or coordinates to measure manually."
            return
        }
        _isRecording.value = true
        _ghostPath.value = emptyList()
        lastAutoSampleAt = 0L
        startSensors()
    }

    private fun startSensors() {
        stepCounter.startListening()
        orientationProvider.startListening()
        sensorJob?.cancel()
        sensorJob = viewModelScope.launch {
            launch { orientationProvider.azimuth.collect { _currentHeading.value = it } }
            launch {
                stepCounter.steps.collect {
                    val project = currentProject.value ?: return@collect
                    val previous = _currentPosition.value ?: return@collect
                    val position = moveSurveyPosition(previous, _currentHeading.value, project.stepLengthMeters,
                        project.widthMeters, project.heightMeters, project.mapTopBearingDegrees)
                    _currentPosition.value = position
                    _ghostPath.value = (_ghostPath.value + (position.x to position.y)).takeLast(100)
                    val now = com.pulsewave.app.core.util.nowMillis()
                    if (_surveyMode.value == SurveyMode.AUTO_SAMPLE && now - lastAutoSampleAt >= 30_000) {
                        lastAutoSampleAt = now
                        addPoint(position.x, position.y, automatic = true)
                    }
                }
            }
        }
    }

    /** A walk belongs to this visible screen; leaving it stops sensors and pending samples. */
    fun onScreenStopped() {
        stopRecording()
        contextGeneration++
    }

    fun stopRecording() {
        if (sensorJob != null) {
            sensorJob?.cancel()
            sensorJob = null
            stepCounter.stopListening()
            orientationProvider.stopListening()
        }
        _isRecording.value = false
    }

    private fun resetPosition() {
        _currentPosition.value = null
        _ghostPath.value = emptyList()
    }

    fun setStartPoint(x: Float, y: Float) { addPoint(x, y) }
    fun correctCurrentPosition(x: Float, y: Float) {
        if (x.isFinite() && y.isFinite()) _currentPosition.value = SurveyPosition(x, y).clamped()
    }
    fun addManualAnchor() {
        val position = _currentPosition.value ?: return
        addPoint(position.x, position.y)
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
                val csv = mutableListOf("Timestamp,X_Ratio,Y_Ratio,RSSI_dBm,SSID,BSSID,Floor_ID,Map_Width_m,Map_Height_m,Map_Top_Bearing_deg")
                points.forEach { p -> csv += listOf(p.timestamp, p.x, p.y, p.rssi, csvField(p.ssid ?: ""),
                    csvField(p.bssid ?: ""), p.floorId, project.widthMeters, project.heightMeters, project.mapTopBearingDegrees).joinToString(",") }
                val safeName = project.name.replace(Regex("[^A-Za-z0-9._-]+"), "_")
                exportManager.exportToCsv(csv, "Survey_${safeName}_${com.pulsewave.app.core.util.nowMillis()}")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) { _message.value = "Could not export this project. Try again." }
        }
    }

    override fun onCleared() {
        stopRecording()
        sampleRequests.close()
        super.onCleared()
    }
}
