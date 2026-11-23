package com.uc.labseat.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.uc.labseat.data.Session
import com.uc.labseat.model.AppUser

/** Shared behaviour: guard against a missing login and handle the Up button. */
abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var user: AppUser

    /** Call at the start of onCreate; if false, return immediately. */
    protected fun ensureLoggedIn(): Boolean {
        val current = Session.user
        if (current == null) {
            goToLogin()
            return false
        }
        user = current
        return true
    }

    protected fun goToLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }

    protected fun showUpButton() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    protected fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
