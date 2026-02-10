package com.pulsewave.app.feature.survey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsewave.app.core.database.dao.PulseWaveDao
import com.pulsewave.app.core.database.entities.SurveyPointEntity
import com.pulsewave.app.core.database.entities.SurveyProjectEntity
import com.pulsewave.app.core.wifi.WifiScanner
import com.pulsewave.app.core.export.ExportManager
import com.pulsewave.app.core.sensors.StepCounter
import com.pulsewave.app.core.sensors.OrientationProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SurveyViewModel(
    private val wifiScanner: WifiScanner,
    private val dao: PulseWaveDao,
    private val exportManager: ExportManager
) : ViewModel(), KoinComponent {

    private val stepCounter: StepCounter by inject()
    private val orientationProvider: OrientationProvider by inject()

    enum class SurveyMode { AUTO_SAMPLE, MANUAL_ANCHOR }

    private val _projects = dao.getAllProjects()
    val projects: StateFlow<List<SurveyProjectEntity>> = _projects.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private val _currentProjectId = MutableStateFlow<Long?>(null)
    private val _selectedFloor = MutableStateFlow(1)
    val selectedFloor = _selectedFloor.asStateFlow()

    val currentPoints: StateFlow<List<SurveyPointEntity>> = combine(_currentProjectId, _selectedFloor, dao.getAllPoints()) { id, floor, allPoints ->
        if (id == null) {
            emptyList()
        } else {
            allPoints.filter { it.projectId == id && it.floorId == floor }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _surveyMode = MutableStateFlow(SurveyMode.AUTO_SAMPLE)
    val surveyMode = _surveyMode.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _currentHeading = MutableStateFlow(0f)
    val currentHeading: StateFlow<Float> = _currentHeading.asStateFlow()

    private val _ghostPath = MutableStateFlow<List<Pair<Float, Float>>>(emptyList())
    val ghostPath: StateFlow<List<Pair<Float, Float>>> = _ghostPath.asStateFlow()

    private var currentX = 0f
    private var currentY = 0f
    private val stepScale = 0.05f

    fun createProject(name: String) {
        viewModelScope.launch {
            val id = dao.insertSurveyProject(
                SurveyProjectEntity(
                    name = name,
                    description = null,
                    imagePath = null,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
            _currentProjectId.value = id
        }
    }

    fun selectProject(id: Long) {
        _currentProjectId.value = id
    }

    fun setFloor(floor: Int) {
        _selectedFloor.value = floor
        _ghostPath.value = emptyList()
    }

    fun addPoint(x: Float, y: Float) {
        val projectId = _currentProjectId.value ?: return
        viewModelScope.launch {
            val latestResults = wifiScanner.scanResults.first()
            val strongest = latestResults.maxByOrNull { it.rssi }

            dao.insertSurveyPoint(
                SurveyPointEntity(
                    projectId = projectId,
                    x = x,
                    y = y,
                    rssi = strongest?.rssi ?: -100,
                    ssid = strongest?.ssid,
                    bssid = strongest?.bssid,
                    floorId = _selectedFloor.value,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteProject(project: SurveyProjectEntity) {
        viewModelScope.launch {
            dao.deleteProject(project)
            if (_currentProjectId.value == project.id) {
                _currentProjectId.value = null
            }
        }
    }

    fun setSurveyMode(mode: SurveyMode) {
        if (!_isRecording.value) {
            _surveyMode.value = mode
        }
    }

    fun toggleRecording() {
        val recording = !_isRecording.value
        _isRecording.value = recording

        if (recording) {
            _ghostPath.value = emptyList()
            startSensors()
        } else {
            stopSensors()
        }
    }

    private fun startSensors() {
        stepCounter.startListening()
        orientationProvider.startListening()

        viewModelScope.launch {
            launch {
                orientationProvider.azimuth.collect { azimuth ->
                    _currentHeading.value = azimuth
                }
            }

            launch {
                stepCounter.steps.collect { _ ->
                    val headingRad = Math.toRadians(_currentHeading.value.toDouble())
                    val dx = (Math.sin(headingRad) * stepScale).toFloat()
                    val dy = (-Math.cos(headingRad) * stepScale).toFloat()

                    var newX = currentX + dx
                    var newY = currentY + dy

                    if (currentX == 0f && currentY == 0f && currentPoints.value.isNotEmpty()) {
                        val last = currentPoints.value.last()
                        newX = last.x + dx
                        newY = last.y + dy
                    } else if (currentX == 0f && currentY == 0f) {
                        newX = 0.5f
                        newY = 0.5f
                    }

                    newX = newX.coerceIn(0f, 1f)
                    newY = newY.coerceIn(0f, 1f)

                    currentX = newX
                    currentY = newY

                    if (_surveyMode.value == SurveyMode.AUTO_SAMPLE) {
                        addPoint(newX, newY)
                    } else {
                        val currentGhost = _ghostPath.value.toMutableList()
                        currentGhost.add(newX to newY)
                        if (currentGhost.size > 100) currentGhost.removeAt(0)
                        _ghostPath.value = currentGhost
                    }
                }
            }
        }
    }

    private fun stopSensors() {
        stepCounter.stopListening()
        orientationProvider.stopListening()
    }

    fun setStartPoint(x: Float, y: Float) {
        currentX = x
        currentY = y
        addPoint(x, y)
    }

    fun correctCurrentPosition(x: Float, y: Float) {
        currentX = x.coerceIn(0f, 1f)
        currentY = y.coerceIn(0f, 1f)

        if (_surveyMode.value == SurveyMode.MANUAL_ANCHOR) {
            val currentGhost = _ghostPath.value.toMutableList()
            if (currentGhost.isNotEmpty()) {
                currentGhost.add(currentX to currentY)
                _ghostPath.value = currentGhost
            }
        }
    }

    fun addManualAnchor() {
        addPoint(currentX, currentY)
    }

    fun exportProject() {
        val projectVal = _currentProjectId.value
        if (projectVal == null) return

        viewModelScope.launch {
            val project = dao.getAllProjects().first().find { it.id == projectVal } ?: return@launch
            val points = dao.getPointsForProject(projectVal).first()

            if (points.isEmpty()) return@launch

            val csvLines = mutableListOf<String>()
            csvLines.add("Timestamp,X_Ratio,Y_Ratio,RSSI_dBm,SSID,BSSID,Floor_ID")

            points.forEach { p ->
                val line = "${p.timestamp},${p.x},${p.y},${p.rssi},\"${p.ssid ?: ""}\",\"${p.bssid ?: ""}\",0"
                csvLines.add(line)
            }

            val filename = "Survey_${project.name.replace(" ", "_")}_${System.currentTimeMillis()}"

            exportManager.exportToCsv(csvLines, filename)
        }
    }
}
