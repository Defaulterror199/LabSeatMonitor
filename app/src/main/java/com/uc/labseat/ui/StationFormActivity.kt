package com.uc.labseat.ui

import android.os.Bundle
import com.uc.labseat.data.LabRepository
import com.uc.labseat.databinding.ActivityStationFormBinding
import com.uc.labseat.model.ActionResult
import com.uc.labseat.model.Role
import com.uc.labseat.util.toast

/** Screen 5 (staff only) - add or edit a station. */
class StationFormActivity : BaseActivity() {

    companion object {
        const val EXTRA_LAB_ID = "extra_lab_id"
        const val EXTRA_STATION_ID = "extra_station_id"
    }

    private lateinit var binding: ActivityStationFormBinding
    private var labId: String = ""
    private var editingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!ensureLoggedIn()) return
        if (user.role != Role.STAFF) {
            finish()
            return
        }

        editingId = intent.getStringExtra(EXTRA_STATION_ID)
        val existing = editingId?.let { LabRepository.getStation(it) }
        labId = existing?.labId ?: intent.getStringExtra(EXTRA_LAB_ID).orEmpty()
        if (labId.isEmpty()) {
            finish()
            return
        }

        binding = ActivityStationFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showUpButton()

        if (existing != null) {
            title = "Edit station"
            binding.etLabel.setText(existing.label)
            binding.etSpecs.setText(existing.specs)
        } else {
            title = "Add station"
        }

        binding.btnSave.setOnClickListener { save() }
    }

    private fun save() {
        val label = binding.etLabel.text.toString()
        val specs = binding.etSpecs.text.toString()
        binding.tilLabel.error = null

        val id = editingId
        val result = if (id != null) {
            LabRepository.updateStation(id, label, specs)
        } else {
            LabRepository.addStation(labId, label, specs)
        }

        when (result) {
            is ActionResult.Success -> {
                toast(result.message)
                finish()
            }
            is ActionResult.Failure -> binding.tilLabel.error = result.message
        }
    }
}
