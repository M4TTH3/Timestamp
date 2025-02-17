package org.timestamp.mobile.utility

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import kotlinx.coroutines.tasks.await

suspend fun getIdTokenResult(forceRefresh: Boolean = false): GetTokenResult? =
    FirebaseAuth.getInstance().currentUser?.getIdToken(forceRefresh)?.await()