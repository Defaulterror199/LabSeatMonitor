package com.uc.labseat.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.uc.labseat.R
import com.uc.labseat.data.LabRepository
import com.uc.labseat.data.Session
import com.uc.labseat.databinding.ActivityLabListBinding
import com.uc.labseat.model.Role

/** Screen 2 - list of laboratories with live availability counts. */
class LabListActivity : BaseActivity() {

    private lateinit var binding: ActivityLabListBinding
    private lateinit var adapter: LabAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!ensureLoggedIn()) return
        binding = ActivityLabListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Laboratories"

        val roleText = if (user.role == Role.STAFF) "Laboratory Staff" else "Student"
        binding.tvWelcome.text = "Welcome, ${user.name} ($roleText)"

        adapter = LabAdapter { lab ->
            // Data passing: send the selected lab's id to the next screen
            val intent = Intent(this, SeatListActivity::class.java)
            intent.putExtra(SeatListActivity.EXTRA_LAB_ID, lab.id)
            startActivity(intent)
        }
        binding.rvLabs.layoutManager = LinearLayoutManager(this)
        binding.rvLabs.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) adapter.submit(LabRepository.getLabSummaries())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_lab_list, menu)
        menu.findItem(R.id.action_my_reservations).isVisible = user.role == Role.STUDENT
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_my_reservations -> {
            startActivity(Intent(this, MyReservationsActivity::class.java))
            true
        }
        R.id.action_logout -> {
            Session.user = null
            goToLogin()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
