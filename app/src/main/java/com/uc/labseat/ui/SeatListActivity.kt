package com.uc.labseat.ui

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.uc.labseat.R
import com.uc.labseat.data.LabRepository
import com.uc.labseat.databinding.ActivitySeatListBinding
import com.uc.labseat.model.Role
import com.uc.labseat.model.StationStatus

/** Screen 3 - all stations in one lab, with a status filter. */
class SeatListActivity : BaseActivity() {

    companion object {
        const val EXTRA_LAB_ID = "extra_lab_id"
    }

    private lateinit var binding: ActivitySeatListBinding
    private lateinit var adapter: StationAdapter
    private lateinit var labId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!ensureLoggedIn()) return

        val id = intent.getStringExtra(EXTRA_LAB_ID)
        val lab = id?.let { LabRepository.getLab(it) }
        if (lab == null) {
            finish()
            return
        }
        labId = lab.id

        binding = ActivitySeatListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = lab.name
        showUpButton()

        adapter = StationAdapter { station ->
            val intent = Intent(this, SeatDetailActivity::class.java)
            intent.putExtra(SeatDetailActivity.EXTRA_STATION_ID, station.id)
            startActivity(intent)
        }
        binding.rvStations.layoutManager = GridLayoutManager(this, 2)
        binding.rvStations.adapter = adapter

        binding.chipGroup.setOnCheckedStateChangeListener { _, _ -> refresh() }

        binding.fabAdd.visibility = if (user.role == Role.STAFF) android.view.View.VISIBLE else android.view.View.GONE
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, StationFormActivity::class.java)
            intent.putExtra(StationFormActivity.EXTRA_LAB_ID, labId)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) refresh()
    }

    private fun currentFilter(): StationStatus? = when (binding.chipGroup.checkedChipId) {
        R.id.chipAvailable -> StationStatus.AVAILABLE
        R.id.chipReserved -> StationStatus.RESERVED
        R.id.chipOccupied -> StationStatus.OCCUPIED
        R.id.chipUnavailable -> StationStatus.UNAVAILABLE
        else -> null
    }

    private fun refresh() {
        val c = LabRepository.countByStatus(labId)
        binding.tvSummary.text =
            "Available ${c[StationStatus.AVAILABLE]}  \u2022  Reserved ${c[StationStatus.RESERVED]}  \u2022  " +
                "Occupied ${c[StationStatus.OCCUPIED]}  \u2022  Unavailable ${c[StationStatus.UNAVAILABLE]}"

        val list = LabRepository.getStations(labId, currentFilter())
        adapter.submit(list)
        binding.tvEmpty.visibility = if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }
}
