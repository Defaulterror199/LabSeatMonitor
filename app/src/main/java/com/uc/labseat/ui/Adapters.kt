package com.uc.labseat.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uc.labseat.databinding.ItemLabBinding
import com.uc.labseat.databinding.ItemReservationBinding
import com.uc.labseat.databinding.ItemStationBinding
import com.uc.labseat.model.*
import com.uc.labseat.util.colorRes
import com.uc.labseat.util.showSessionState
import com.uc.labseat.util.showStatus
import com.uc.labseat.util.toDisplayTime
import androidx.core.content.ContextCompat

// ------------------------------------------------------------------ Labs

class LabAdapter(private val onClick: (Lab) -> Unit) :
    RecyclerView.Adapter<LabAdapter.VH>() {

    private var items: List<LabSummary> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun submit(list: List<LabSummary>) {
        items = list
        notifyDataSetChanged()
    }

    class VH(val b: ItemLabBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemLabBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val c = item.counts
        holder.b.tvLabName.text = item.lab.name
        holder.b.tvLabRoom.text = item.lab.room
        holder.b.tvLabAvailability.text = "${item.available} of ${item.total} stations available"
        holder.b.tvLabBreakdown.text =
            "Reserved ${c[StationStatus.RESERVED] ?: 0}  \u2022  " +
                "Occupied ${c[StationStatus.OCCUPIED] ?: 0}  \u2022  " +
                "Unavailable ${c[StationStatus.UNAVAILABLE] ?: 0}"
        holder.b.root.setOnClickListener { onClick(item.lab) }
    }
}

// -------------------------------------------------------------- Stations

class StationAdapter(private val onClick: (Station) -> Unit) :
    RecyclerView.Adapter<StationAdapter.VH>() {

    private var items: List<Station> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun submit(list: List<Station>) {
        items = list
        notifyDataSetChanged()
    }

    class VH(val b: ItemStationBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemStationBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val s = items[position]
        val ctx = holder.b.root.context
        holder.b.tvStationLabel.text = s.label
        holder.b.tvStationSpecs.text = s.specs
        holder.b.tvStationStatus.showStatus(s.status)
        holder.b.vStripe.setBackgroundColor(ContextCompat.getColor(ctx, s.status.colorRes()))
        holder.b.tvStationHolder.text = when {
            s.heldBy.isNotEmpty() -> "Held by: ${s.heldBy}"
            s.note.isNotEmpty() -> "Note: ${s.note}"
            else -> ""
        }
        holder.b.root.setOnClickListener { onClick(s) }
    }
}

// ---------------------------------------------------------- Reservations

class ReservationAdapter : RecyclerView.Adapter<ReservationAdapter.VH>() {

    private var items: List<Reservation> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun submit(list: List<Reservation>) {
        items = list
        notifyDataSetChanged()
    }

    class VH(val b: ItemReservationBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemReservationBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = items[position]
        holder.b.tvResStation.text = "${r.stationLabel}  \u2022  ${r.labName}"
        holder.b.tvResState.showSessionState(r.state)
        holder.b.tvResTime.text =
            "Started: ${r.startedAt.toDisplayTime()}" +
                if (r.endedAt > 0L) "\nEnded: ${r.endedAt.toDisplayTime()}" else ""
    }
}
