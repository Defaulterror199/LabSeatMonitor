package com.uc.labseat.util

import android.content.Context
import android.content.res.ColorStateList
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.uc.labseat.R
import com.uc.labseat.model.SessionState
import com.uc.labseat.model.StationStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Context.toast(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Long.toDisplayTime(): String =
    if (this <= 0L) "-" else SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(this))

fun StationStatus.colorRes(): Int = when (this) {
    StationStatus.AVAILABLE -> R.color.status_available
    StationStatus.RESERVED -> R.color.status_reserved
    StationStatus.OCCUPIED -> R.color.status_occupied
    StationStatus.UNAVAILABLE -> R.color.status_unavailable
}

fun SessionState.colorRes(): Int = when (this) {
    SessionState.RESERVED -> R.color.status_reserved
    SessionState.IN_USE -> R.color.status_occupied
    SessionState.COMPLETED -> R.color.status_available
    SessionState.CANCELLED -> R.color.status_unavailable
}

/** Shows a station status as a colored badge (TextView must use @drawable/bg_badge). */
fun TextView.showStatus(status: StationStatus) {
    text = status.label
    backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, status.colorRes()))
}

fun TextView.showSessionState(state: SessionState) {
    text = state.label
    backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, state.colorRes()))
}
