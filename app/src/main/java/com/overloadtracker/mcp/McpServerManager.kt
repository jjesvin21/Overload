package com.overloadtracker.mcp

import com.overloadtracker.data.model.McpAuthRequest
import com.overloadtracker.data.model.McpAuthResponse
import com.overloadtracker.data.model.McpExerciseDto
import com.overloadtracker.data.model.McpNewSplitDto
import com.overloadtracker.data.model.McpReplaceSplitsRequest
import com.overloadtracker.data.model.McpReplaceSplitsResponse
import com.overloadtracker.data.model.McpSessionSetDto
import com.overloadtracker.data.model.McpSplitDto
import com.overloadtracker.data.model.McpStatusResponse
import com.overloadtracker.data.model.McpWorkoutHistoryResponse
import com.overloadtracker.data.model.McpWorkoutSessionDto
import com.overloadtracker.data.repository.ExerciseRepository
import com.overloadtracker.data.repository.NewSplitRequest
import com.overloadtracker.data.repository.WorkoutGroupRepository
import com.overloadtracker.data.repository.WorkoutSessionRepository
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.header
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class McpServerManager @Inject constructor(
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val workoutGroupRepository: WorkoutGroupRepository,
    private val exerciseRepository: ExerciseRepository
) {
    private var serverEngine: CIOApplicationEngine? = null
    private var runningPort: Int = 8080
    private var runningToken: String = ""
    private var bindLocalOnly: Boolean = false
    private val activeSessions = ConcurrentHashMap<String, Long>()

    val isRunning: Boolean get() = serverEngine != null

    fun revokeAllSessions() {
        activeSessions.clear()
    }

    fun startServer(port: Int = 8080, token: String = "", bindLocalOnly: Boolean = false) {
        if (serverEngine != null) return
        runningPort = port
        runningToken = token
        this.bindLocalOnly = bindLocalOnly

        val host = if (bindLocalOnly) "127.0.0.1" else "0.0.0.0"

        CoroutineScope(Dispatchers.IO).launch {
            val engine = embeddedServer(CIO, port = port, host = host) {
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    })
                }
                install(CORS) {
                    anyHost()
                    allowHeader(HttpHeaders.ContentType)
                    allowHeader(HttpHeaders.Authorization)
                    allowHeader("X-Master-Secret")
                    allowMethod(HttpMethod.Get)
                    allowMethod(HttpMethod.Post)
                    allowMethod(HttpMethod.Options)
                }

                routing {
                    // 1. Status Check
                    get("/api/v1/status") {
                        call.respond(
                            McpStatusResponse(
                                status = "ONLINE",
                                activeIpAddress = getLocalIpAddress(),
                                port = runningPort
                            )
                        )
                    }

                    // 1b. Handshake Auth (Returns 1-Hour Session Token)
                    post("/api/v1/auth") {
                        val headerSecret = call.request.header("X-Master-Secret")
                            ?: call.request.header(HttpHeaders.Authorization)?.removePrefix("Bearer ")?.trim()

                        var bodySecret: String? = null
                        try {
                            val body = call.receive<McpAuthRequest>()
                            bodySecret = body.masterSecret
                        } catch (_: Exception) {}

                        val providedSecret = headerSecret ?: bodySecret

                        if (providedSecret.isNullOrBlank() || providedSecret != runningToken) {
                            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid Master Secret"))
                            return@post
                        }

                        val sessionToken = "ovld_sess_" + java.util.UUID.randomUUID().toString().replace("-", "").take(24)
                        val ttlSeconds = 3600L
                        val expiryTime = System.currentTimeMillis() + (ttlSeconds * 1000)
                        activeSessions[sessionToken] = expiryTime

                        call.respond(
                            McpAuthResponse(
                                status = "AUTHENTICATED",
                                sessionToken = sessionToken,
                                expiresInSeconds = ttlSeconds
                            )
                        )
                    }

                    // 2. Fetch Workout History
                    get("/api/v1/workout-history") {
                        if (!validateAuth(call.request.header(HttpHeaders.Authorization))) {
                            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid Authorization Token"))
                            return@get
                        }
                        val rangeParam = call.request.queryParameters["timeRange"] ?: "ALL_TIME"
                        val cutoffTime = when (rangeParam.uppercase()) {
                            "LAST_7_DAYS" -> System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
                            "LAST_30_DAYS" -> System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                            else -> null
                        }

                        val summaries = workoutSessionRepository.buildCsvRows(startTimeCutoff = cutoffTime)
                        val sessionsWithSets = workoutSessionRepository.observeSessions().first()
                        val allExercises = exerciseRepository.observeAll().first().associateBy { it.id }

                        val sessionDtos = sessionsWithSets
                            .filter { cutoffTime == null || it.endTime >= cutoffTime }
                            .map { session ->
                                val sets = workoutSessionRepository.observeSession(session.id).first()?.sets ?: emptyList()
                                val setDtos = sets.map { s ->
                                    McpSessionSetDto(
                                        exerciseId = s.exerciseId,
                                        exerciseName = s.exerciseName,
                                        setNumber = s.setNumber,
                                        weight = s.weight,
                                        reps = s.reps,
                                        rpe = s.rpe,
                                        restSeconds = s.restSeconds
                                    )
                                }
                                val muscleGroups = sets.mapNotNull { allExercises[it.exerciseId]?.category }.distinct()
                                McpWorkoutSessionDto(
                                    sessionId = session.id,
                                    groupName = session.groupName,
                                    startTime = session.startTime,
                                    endTime = session.endTime,
                                    totalVolume = session.totalVolume,
                                    muscleGroups = muscleGroups,
                                    sets = setDtos
                                )
                            }

                        val totalVolume = sessionDtos.sumOf { it.totalVolume }
                        val muscleBreakdown = mutableMapOf<String, Double>()
                        sessionDtos.forEach { session ->
                            session.sets.forEach { set ->
                                val cat = allExercises[set.exerciseId]?.category ?: "Other"
                                muscleBreakdown[cat] = (muscleBreakdown[cat] ?: 0.0) + (set.weight * set.reps)
                            }
                        }

                        call.respond(
                            McpWorkoutHistoryResponse(
                                totalSessions = sessionDtos.size,
                                totalVolumeKg = totalVolume,
                                muscleGroupBreakdown = muscleBreakdown,
                                sessions = sessionDtos
                            )
                        )
                    }

                    // 3. Fetch Exercise Library
                    get("/api/v1/exercise-library") {
                        if (!validateAuth(call.request.header(HttpHeaders.Authorization))) {
                            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid Authorization Token"))
                            return@get
                        }
                        val category = call.request.queryParameters["category"]
                        val exercises = exerciseRepository.observeAll().first().filter {
                            category == null || it.category.equals(category, ignoreCase = true)
                        }.map {
                            McpExerciseDto(
                                id = it.id,
                                name = it.name,
                                category = it.category,
                                equipment = it.equipment,
                                instructions = it.instructions
                            )
                        }
                        call.respond(exercises)
                    }

                    // 4. Fetch Current Splits
                    get("/api/v1/splits") {
                        if (!validateAuth(call.request.header(HttpHeaders.Authorization))) {
                            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid Authorization Token"))
                            return@get
                        }
                        val groupsWithExercises = workoutGroupRepository.getAllGroupsWithExercisesSync()
                        val allExercises = exerciseRepository.observeAll().first().associateBy { it.id }

                        val splitDtos = groupsWithExercises.map { (group, groupExercises) ->
                            val exercises = groupExercises.mapNotNull { ge ->
                                allExercises[ge.exerciseId]?.let { ex ->
                                    McpExerciseDto(
                                        id = ex.id,
                                        name = ex.name,
                                        category = ex.category,
                                        equipment = ex.equipment,
                                        instructions = ex.instructions
                                    )
                                }
                            }
                            McpSplitDto(
                                id = group.id,
                                name = group.name,
                                notes = group.notes,
                                exercises = exercises
                            )
                        }
                        call.respond(splitDtos)
                    }

                    // 5. Replace Splits (Atomic Wipe and Overwrite)
                    post("/api/v1/splits/replace") {
                        if (!validateAuth(call.request.header(HttpHeaders.Authorization))) {
                            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid Authorization Token"))
                            return@post
                        }
                        val request = call.receive<McpReplaceSplitsRequest>()
                        if (!request.confirmReplace) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "confirmReplace must be true"))
                            return@post
                        }

                        val domainRequests = request.splits.map {
                            NewSplitRequest(
                                name = it.name,
                                notes = it.notes,
                                exerciseIds = it.exerciseIds
                            )
                        }
                        val createdIds = workoutGroupRepository.replaceAllSplits(domainRequests)

                        call.respond(
                            McpReplaceSplitsResponse(
                                status = "SUCCESS",
                                message = "Successfully replaced existing splits with ${createdIds.size} new splits.",
                                createdSplitsCount = createdIds.size,
                                newSplitIds = createdIds
                            )
                        )
                    }
                }
            }
            serverEngine = engine
            engine.start(wait = true)
        }
    }

    fun stopServer() {
        serverEngine?.stop(1000, 2000)
        serverEngine = null
    }

    private fun validateAuth(authHeader: String?): Boolean {
        if (runningToken.isBlank()) return true
        if (authHeader.isNullOrBlank()) return false
        val token = authHeader.removePrefix("Bearer ").trim()

        // 1. Direct Master Secret match
        if (token == runningToken) return true

        // 2. Active Session Token match
        val expiryTime = activeSessions[token] ?: return false
        if (System.currentTimeMillis() > expiryTime) {
            activeSessions.remove(token)
            return false
        }
        return true
    }

    fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress
                    }
                }
            }
        } catch (_: Exception) {}
        return null
    }
}
