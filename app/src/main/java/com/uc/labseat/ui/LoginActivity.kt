package com.uc.labseat.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.uc.labseat.data.Session
import com.uc.labseat.databinding.ActivityLoginBinding
import com.uc.labseat.model.AppUser
import com.uc.labseat.model.Role

/**
 * Screen 1 - Phase 1 uses a local sign-in form (first name, last name, ID number, role).
 * Phase 2 replaces it with Firebase Authentication.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val namePattern = Regex("^[\\p{L}][\\p{L} .'-]+$")

    private val idPattern = Regex("^\\d{6,10}$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnContinue.setOnClickListener { attemptContinue() }
    }

    private fun clean(raw: CharSequence?): String =
        raw?.toString().orEmpty().trim().replace(Regex("\\s+"), " ")
            .split(" ")
            .joinToString(" ") { w -> w.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase() else c.toString() } }

    private fun attemptContinue() {
        val first = clean(binding.etFirstName.text)
        val last = clean(binding.etLastName.text)
        val id = binding.etIdNumber.text.toString().trim()

        var valid = true
        if (!namePattern.matches(first)) {
            binding.tilFirstName.error = "Enter your first name (letters only, at least 2 characters)"
            valid = false
        } else binding.tilFirstName.error = null

        if (!namePattern.matches(last)) {
            binding.tilLastName.error = "Enter your last name (letters only, at least 2 characters)"
            valid = false
        } else binding.tilLastName.error = null

        if (!idPattern.matches(id)) {
            binding.tilIdNumber.error = "Enter your ID number (6 to 10 digits)"
            valid = false
        } else binding.tilIdNumber.error = null

        if (!valid) return

        val role = if (binding.rbStaff.isChecked) Role.STAFF else Role.STUDENT
        Session.user = AppUser(first, last, id, role)

        startActivity(Intent(this, LabListActivity::class.java))
        finish()
    }
}