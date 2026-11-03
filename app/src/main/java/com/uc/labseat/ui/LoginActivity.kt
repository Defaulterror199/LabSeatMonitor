package com.uc.labseat.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.uc.labseat.data.Session
import com.uc.labseat.databinding.ActivityLoginBinding
import com.uc.labseat.model.AppUser
import com.uc.labseat.model.Role

/** Screen 1 - Phase 1 uses a local role picker. Phase 2 replaces it with Firebase Authentication. */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnContinue.setOnClickListener { attemptContinue() }
    }

    private fun attemptContinue() {
        val name = binding.etName.text.toString().trim()
        if (name.length < 2) {
            binding.tilName.error = "Enter your full name (at least 2 characters)"
            return
        }
        binding.tilName.error = null

        val role = if (binding.rbStaff.isChecked) Role.STAFF else Role.STUDENT
        Session.user = AppUser(name, role)

        startActivity(Intent(this, LabListActivity::class.java))
        finish()
    }
}
