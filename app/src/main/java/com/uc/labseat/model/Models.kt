package com.uc.labseat.model

// NOTE: every property has a default value so these classes can be read back
// from Firebase later (Firebase needs a no-argument constructor).

enum class Role { STUDENT, STAFF }

data class AppUser(
    val firstName: String = "",
    val lastName: String = "",
    val idNumber: String = "",
    val role: Role = Role.STUDENT
) {
    val fullName: String get() = "$firstName $lastName".trim()
}

/** Current state of one computer station. This is the "must have a current state" rule. */
enum class StationStatus(val label: String) {
    AVAILABLE("Available"),
    RESERVED("Reserved"),
    OCCUPIED("Occupied"),
    UNAVAILABLE("Unavailable")
}

data class Lab(
    val id: String = "",
    val name: String = "",
    val room: String = ""
)

data class Station(
    val id: String = "",
    val labId: String = "",
    val label: String = "",
    val specs: String = "",
    val status: StationStatus = StationStatus.AVAILABLE,
    val heldBy: String = "",
    val heldById: String = "",
    val note: String = "",
    val updatedAt: Long = 0L
)

enum class SessionState(val label: String) {
    RESERVED("Reserved"),
    IN_USE("In use"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

/** History record: one student's reservation / use of one station. */
data class Reservation(
    val id: String = "",
    val stationId: String = "",
    val stationLabel: String = "",
    val labName: String = "",
    val studentName: String = "",
    val studentId: String = "",
    val state: SessionState = SessionState.RESERVED,
    val startedAt: Long = 0L,
    val endedAt: Long = 0L
)

/** Everything a user can try to do to a station. The rules live in LabRepository. */
enum class SeatAction(val label: String) {
    RESERVE("Reserve this station"),
    CHECK_IN("Check in / start using"),
    RELEASE("Release station (finished)"),
    CANCEL("Cancel reservation"),
    MARK_UNAVAILABLE("Mark as unavailable"),
    RESTORE("Restore to available")
}

sealed class ActionResult {
    abstract val message: String
    data class Success(override val message: String) : ActionResult()
    data class Failure(override val message: String) : ActionResult()
}

/** Row model for the lab list screen. */
data class LabSummary(
    val lab: Lab,
    val counts: Map<StationStatus, Int>
) {
    val total: Int get() = counts.values.sum()
    val available: Int get() = counts[StationStatus.AVAILABLE] ?: 0
}