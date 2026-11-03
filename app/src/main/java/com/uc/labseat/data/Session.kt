package com.uc.labseat.data

import com.uc.labseat.model.AppUser

/** Holds the logged-in user in memory. Phase 2 replaces the source with Firebase Auth. */
object Session {
    var user: AppUser? = null
}
