package com.uc.labseat.data

import com.uc.labseat.model.*

/**
 * Phase 1 local data source (in-memory mock data).
 *
 * All business rules for station state live in [check] so the UI and the future
 * Firebase version share ONE source of truth. In Phase 2/3 only the storage
 * calls change (list -> Firebase); the rules stay.
 */
object LabRepository {

    private val labs = mutableListOf<Lab>()
    private val stations = mutableListOf<Station>()
    private val sessions = mutableListOf<Reservation>()
    private var idCounter = 0

    init {
        seed()
    }

    // ---------------------------------------------------------------- READ

    fun getLabs(): List<Lab> = labs.toList()

    fun getLab(id: String): Lab? = labs.firstOrNull { it.id == id }

    fun getStation(id: String): Station? = stations.firstOrNull { it.id == id }

    fun getStations(labId: String, status: StationStatus? = null): List<Station> =
        stations.filter { it.labId == labId && (status == null || it.status == status) }
            .sortedBy { it.label }

    fun countByStatus(labId: String): Map<StationStatus, Int> {
        val inLab = stations.filter { it.labId == labId }
        return StationStatus.values().associateWith { st -> inLab.count { it.status == st } }
    }

    fun getLabSummaries(): List<LabSummary> =
        labs.map { LabSummary(it, countByStatus(it.id)) }

    fun getReservationsFor(userName: String): List<Reservation> =
        sessions.filter { it.studentName.equals(userName, ignoreCase = true) }
            .sortedByDescending { it.startedAt }

    /** The station a student currently holds (reserved or occupied), if any. */
    fun activeHold(userName: String): Station? =
        stations.firstOrNull {
            it.heldBy.equals(userName, ignoreCase = true) &&
                (it.status == StationStatus.RESERVED || it.status == StationStatus.OCCUPIED)
        }

    // ------------------------------------------------- BUSINESS RULES (state)

    fun availableActions(station: Station, user: AppUser): List<SeatAction> =
        SeatAction.values().filter { check(it, station, user) is ActionResult.Success }

    /** Why nothing (or less) can be done - shown to the user as a hint. */
    fun hintFor(station: Station, user: AppUser): String {
        val isStaff = user.role == Role.STAFF
        return when (station.status) {
            StationStatus.OCCUPIED ->
                if (isStaff || station.heldBy.equals(user.name, true)) ""
                else "In use by ${station.heldBy}. It cannot be assigned to anyone else until it is released."
            StationStatus.RESERVED ->
                if (isStaff || station.heldBy.equals(user.name, true)) ""
                else "Reserved by ${station.heldBy}. It cannot be taken by anyone else."
            StationStatus.UNAVAILABLE ->
                if (isStaff) "" else "Temporarily unavailable. Only laboratory staff can restore it."
            StationStatus.AVAILABLE -> {
                val hold = if (isStaff) null else activeHold(user.name)
                if (hold != null)
                    "You already hold ${hold.label}. Release or cancel it before taking another station."
                else ""
            }
        }
    }

    private fun ok(msg: String = "") = ActionResult.Success(msg)
    private fun fail(msg: String) = ActionResult.Failure(msg)

    /** The single place that decides whether an action is allowed. */
    private fun check(action: SeatAction, s: Station, user: AppUser): ActionResult {
        val isStaff = user.role == Role.STAFF
        val isHolder = s.heldBy.equals(user.name, ignoreCase = true)
        return when (action) {
            SeatAction.RESERVE -> when {
                isStaff -> fail("Only students can reserve a station.")
                s.status != StationStatus.AVAILABLE -> fail("Only an available station can be reserved.")
                activeHold(user.name) != null -> fail("You already hold a station.")
                else -> ok()
            }
            SeatAction.CHECK_IN -> when {
                s.status == StationStatus.AVAILABLE && !isStaff ->
                    if (activeHold(user.name) != null) fail("You already hold a station.") else ok()
                s.status == StationStatus.RESERVED && (isHolder || isStaff) -> ok()
                else -> fail("This station cannot be checked in right now.")
            }
            SeatAction.RELEASE ->
                if (s.status == StationStatus.OCCUPIED && (isHolder || isStaff)) ok()
                else fail("Only the occupant or staff can release an occupied station.")
            SeatAction.CANCEL ->
                if (s.status == StationStatus.RESERVED && (isHolder || isStaff)) ok()
                else fail("Only the reserving student or staff can cancel a reservation.")
            SeatAction.MARK_UNAVAILABLE -> when {
                !isStaff -> fail("Only laboratory staff can mark a station unavailable.")
                s.status != StationStatus.AVAILABLE ->
                    fail("Release or cancel the current reservation/use first.")
                else -> ok()
            }
            SeatAction.RESTORE ->
                if (isStaff && s.status == StationStatus.UNAVAILABLE) ok()
                else fail("Only staff can restore an unavailable station.")
        }
    }

    fun perform(action: SeatAction, stationId: String, user: AppUser, reason: String = ""): ActionResult {
        val s = getStation(stationId) ?: return fail("This station no longer exists.")
        val verdict = check(action, s, user)
        if (verdict is ActionResult.Failure) return verdict

        val now = System.currentTimeMillis()
        return when (action) {
            SeatAction.RESERVE -> {
                replace(s.copy(status = StationStatus.RESERVED, heldBy = user.name, note = "", updatedAt = now))
                sessions += newSession(s, user.name, SessionState.RESERVED, now)
                ok("${s.label} reserved for you.")
            }
            SeatAction.CHECK_IN -> {
                val holder = if (s.status == StationStatus.RESERVED) s.heldBy else user.name
                replace(s.copy(status = StationStatus.OCCUPIED, heldBy = holder, updatedAt = now))
                if (!updateSession(s.id, SessionState.IN_USE, 0L)) {
                    sessions += newSession(s, holder, SessionState.IN_USE, now)
                }
                ok("${s.label} is now in use by $holder.")
            }
            SeatAction.RELEASE -> {
                replace(s.copy(status = StationStatus.AVAILABLE, heldBy = "", updatedAt = now))
                updateSession(s.id, SessionState.COMPLETED, now)
                ok("${s.label} released and available again.")
            }
            SeatAction.CANCEL -> {
                replace(s.copy(status = StationStatus.AVAILABLE, heldBy = "", updatedAt = now))
                updateSession(s.id, SessionState.CANCELLED, now)
                ok("Reservation for ${s.label} cancelled.")
            }
            SeatAction.MARK_UNAVAILABLE -> {
                val why = reason.trim()
                if (why.length < 3) return fail("Please enter a reason (at least 3 characters).")
                replace(s.copy(status = StationStatus.UNAVAILABLE, heldBy = "", note = why, updatedAt = now))
                ok("${s.label} marked unavailable.")
            }
            SeatAction.RESTORE -> {
                replace(s.copy(status = StationStatus.AVAILABLE, note = "", updatedAt = now))
                ok("${s.label} restored to available.")
            }
        }
    }

    // ---------------------------------------------------- STATION CRUD (staff)

    fun addStation(labId: String, label: String, specs: String): ActionResult {
        val clean = label.trim().uppercase()
        validateLabel(labId, clean, null)?.let { return it }
        stations += Station(
            id = newId("station"), labId = labId, label = clean,
            specs = specs.trim(), status = StationStatus.AVAILABLE,
            updatedAt = System.currentTimeMillis()
        )
        return ok("Station $clean added.")
    }

    fun updateStation(stationId: String, label: String, specs: String): ActionResult {
        val s = getStation(stationId) ?: return fail("This station no longer exists.")
        val clean = label.trim().uppercase()
        validateLabel(s.labId, clean, s.id)?.let { return it }
        replace(s.copy(label = clean, specs = specs.trim(), updatedAt = System.currentTimeMillis()))
        return ok("Station updated.")
    }

    fun deleteStation(stationId: String): ActionResult {
        val s = getStation(stationId) ?: return fail("This station no longer exists.")
        if (s.status == StationStatus.RESERVED || s.status == StationStatus.OCCUPIED)
            return fail("A reserved or occupied station cannot be deleted. Release or cancel it first.")
        stations.removeAll { it.id == stationId }
        return ok("Station ${s.label} deleted.")
    }

    private fun validateLabel(labId: String, label: String, ignoreId: String?): ActionResult? {
        if (label.isEmpty()) return fail("Station label is required.")
        if (label.length > 10) return fail("Station label must be 10 characters or fewer.")
        val duplicate = stations.any {
            it.labId == labId && it.label.equals(label, ignoreCase = true) && it.id != ignoreId
        }
        if (duplicate) return fail("$label already exists in this laboratory.")
        return null
    }

    // ------------------------------------------------------------- helpers

    private fun newId(prefix: String) = "$prefix-${++idCounter}"

    private fun replace(updated: Station) {
        val i = stations.indexOfFirst { it.id == updated.id }
        if (i >= 0) stations[i] = updated
    }

    private fun newSession(s: Station, holder: String, state: SessionState, now: Long): Reservation {
        val labName = getLab(s.labId)?.name ?: ""
        return Reservation(newId("res"), s.id, s.label, labName, holder, state, now, 0L)
    }

    /** Moves the latest open (RESERVED / IN_USE) session of a station to a new state. */
    private fun updateSession(stationId: String, newState: SessionState, endedAt: Long): Boolean {
        val i = sessions.indexOfLast {
            it.stationId == stationId &&
                (it.state == SessionState.RESERVED || it.state == SessionState.IN_USE)
        }
        if (i < 0) return false
        sessions[i] = sessions[i].copy(state = newState, endedAt = endedAt)
        return true
    }

    // ------------------------------------------------------------ mock data

    private class Preset(val status: StationStatus, val text: String)

    private fun seed() {
        labs += Lab("lab1", "Computer Laboratory 1", "Room 301")
        labs += Lab("lab2", "Computer Laboratory 2", "Room 302")
        labs += Lab("lab3", "Multimedia Laboratory", "Room 405")

        seedLab("lab1", 12, "Core i5 | 16 GB RAM", mapOf(
            2 to Preset(StationStatus.OCCUPIED, "Maria Santos"),
            3 to Preset(StationStatus.OCCUPIED, "Juan Dela Cruz"),
            5 to Preset(StationStatus.RESERVED, "Ana Reyes"),
            7 to Preset(StationStatus.UNAVAILABLE, "Keyboard missing"),
            8 to Preset(StationStatus.OCCUPIED, "Paolo Garcia"),
            11 to Preset(StationStatus.RESERVED, "Liza Tan")
        ))
        seedLab("lab2", 8, "Core i7 | 32 GB RAM | GPU", mapOf(
            1 to Preset(StationStatus.OCCUPIED, "Carlo Mendoza"),
            4 to Preset(StationStatus.UNAVAILABLE, "OS update in progress"),
            6 to Preset(StationStatus.RESERVED, "Grace Lim")
        ))
        seedLab("lab3", 6, "Core i5 | 16 GB RAM | Drawing tablet", mapOf(
            3 to Preset(StationStatus.OCCUPIED, "Kevin Uy")
        ))
    }

    private fun seedLab(labId: String, count: Int, specs: String, presets: Map<Int, Preset>) {
        val lab = labs.first { it.id == labId }
        val now = System.currentTimeMillis()
        for (i in 1..count) {
            val label = "PC-%02d".format(i)
            val p = presets[i]
            val status = p?.status ?: StationStatus.AVAILABLE
            val holder = if (status == StationStatus.RESERVED || status == StationStatus.OCCUPIED) p?.text.orEmpty() else ""
            val note = if (status == StationStatus.UNAVAILABLE) p?.text.orEmpty() else ""
            val st = Station(
                id = "$labId-pc%02d".format(i), labId = labId, label = label, specs = specs,
                status = status, heldBy = holder, note = note, updatedAt = now - 15 * 60_000L
            )
            stations += st
            if (status == StationStatus.RESERVED)
                sessions += Reservation(newId("res"), st.id, label, lab.name, holder, SessionState.RESERVED, now - 10 * 60_000L, 0L)
            if (status == StationStatus.OCCUPIED)
                sessions += Reservation(newId("res"), st.id, label, lab.name, holder, SessionState.IN_USE, now - 40 * 60_000L, 0L)
        }
    }
}
