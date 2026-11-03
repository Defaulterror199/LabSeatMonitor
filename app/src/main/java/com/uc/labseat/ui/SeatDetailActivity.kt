package com.uc.labseat.ui

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.uc.labseat.data.LabRepository
import com.uc.labseat.databinding.ActivitySeatDetailBinding
import com.uc.labseat.model.ActionResult
import com.uc.labseat.model.Role
import com.uc.labseat.model.SeatAction
import com.uc.labseat.util.showStatus
import com.uc.labseat.util.toDisplayTime
import com.uc.labseat.util.toast

/** Screen 4 - one station: current state + the actions this user is allowed to take. */
class SeatDetailActivity : BaseActivity() {

    companion object {
        const val EXTRA_STATION_ID = "extra_station_id"
    }

    private lateinit var binding: ActivitySeatDetailBinding
    private lateinit var stationId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!ensureLoggedIn()) return

        val id = intent.getStringExtra(EXTRA_STATION_ID)
        if (id == null) {
            finish()
            return
        }
        stationId = id

        binding = ActivitySeatDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Station details"
        showUpButton()

        val isStaff = user.role == Role.STAFF
        binding.staffPanel.visibility = if (isStaff) View.VISIBLE else View.GONE
        binding.btnEdit.setOnClickListener {
            val intent = Intent(this, StationFormActivity::class.java)
            intent.putExtra(StationFormActivity.EXTRA_STATION_ID, stationId)
            startActivity(intent)
        }
        binding.btnDelete.setOnClickListener { confirmDelete() }
    }

    override fun onResume() {
        super.onResume()
        if (::binding.isInitialized) render()
    }

    private fun render() {
        val s = LabRepository.getStation(stationId)
        if (s == null) {
            finish()
            return
        }
        val lab = LabRepository.getLab(s.labId)

        binding.tvDetailLabel.text = s.label
        binding.tvDetailLab.text = lab?.let { "${it.name} - ${it.room}" } ?: ""
        binding.tvDetailStatus.showStatus(s.status)
        binding.tvDetailSpecs.text = "Specs: ${s.specs.ifEmpty { "-" }}"
        binding.tvDetailHolder.text = "Held by: ${s.heldBy.ifEmpty { "-" }}"
        binding.tvDetailNote.text = "Note: ${s.note.ifEmpty { "-" }}"
        binding.tvDetailUpdated.text = "Last updated: ${s.updatedAt.toDisplayTime()}"

        val hint = LabRepository.hintFor(s, user)
        binding.tvDetailHint.text = hint
        binding.tvDetailHint.visibility = if (hint.isEmpty()) View.GONE else View.VISIBLE

        // Only the actions allowed by the business rules are shown as buttons
        binding.actionsContainer.removeAllViews()
        for (action in LabRepository.availableActions(s, user)) {
            val btn = MaterialButton(this).apply {
                text = action.label
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).also { it.topMargin = dp(8) }
                setOnClickListener { onActionClicked(action) }
            }
            binding.actionsContainer.addView(btn)
        }
    }

    private fun onActionClicked(action: SeatAction) {
        if (action == SeatAction.MARK_UNAVAILABLE) {
            askReasonThenMarkUnavailable()
        } else {
            handle(LabRepository.perform(action, stationId, user))
        }
    }

    private fun askReasonThenMarkUnavailable() {
        val input = EditText(this).apply {
            hint = "Reason (e.g., keyboard broken)"
            setSingleLine()
            filters = arrayOf(InputFilter.LengthFilter(60))
        }
        val holder = FrameLayout(this).apply {
            setPadding(dp(20), dp(8), dp(20), 0)
            addView(input)
        }
        MaterialAlertDialogBuilder(this)
            .setTitle("Mark as unavailable")
            .setView(holder)
            .setPositiveButton("Confirm") { _, _ ->
                handle(LabRepository.perform(SeatAction.MARK_UNAVAILABLE, stationId, user, input.text.toString()))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDelete() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete station?")
            .setMessage("This removes the station from the laboratory.")
            .setPositiveButton("Delete") { _, _ ->
                val result = LabRepository.deleteStation(stationId)
                toast(result.message)
                if (result is ActionResult.Success) finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handle(result: ActionResult) {
        toast(result.message)
        render()
    }
}
