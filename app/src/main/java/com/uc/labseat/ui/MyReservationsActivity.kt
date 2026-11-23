package com.uc.labseat.ui

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.uc.labseat.data.LabRepository
import com.uc.labseat.databinding.ActivityMyReservationsBinding

/** Screen 6 (student) - the student's own reservation / usage history. */
class MyReservationsActivity : BaseActivity() {

    private lateinit var binding: ActivityMyReservationsBinding
    private lateinit var adapter: ReservationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!ensureLoggedIn()) return
        binding = ActivityMyReservationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "My reservations"
        showUpButton()

        adapter = ReservationAdapter()
        binding.rvReservations.layoutManager = LinearLayoutManager(this)
        binding.rvReservations.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        if (!::adapter.isInitialized) return
        val list = LabRepository.getReservationsFor(user.idNumber)
        adapter.submit(list)
        binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }
}