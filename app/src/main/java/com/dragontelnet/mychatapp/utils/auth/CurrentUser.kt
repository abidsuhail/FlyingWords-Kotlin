package com.dragontelnet.mychatapp.utils.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object CurrentUser {

    fun getCurrentUser(): FirebaseUser? = FirebaseAuth.getInstance().currentUser
}