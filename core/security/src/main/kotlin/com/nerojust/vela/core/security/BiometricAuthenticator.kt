package com.nerojust.vela.core.security

import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.Executor

sealed interface BiometricResult {
    data object Success : BiometricResult

    data object Cancelled : BiometricResult

    data class Error(val message: String) : BiometricResult
}

class BiometricAuthenticator(
    private val activity: FragmentActivity,
    private val executor: Executor,
) {
    fun authenticate(
        title: String,
        subtitle: String,
    ): Flow<BiometricResult> =
        callbackFlow {
            val callback =
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        trySend(BiometricResult.Success)
                        close()
                    }

                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence,
                    ) {
                        val cancelled =
                            errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                                errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON
                        trySend(
                            if (cancelled) BiometricResult.Cancelled else BiometricResult.Error(errString.toString()),
                        )
                        close()
                    }
                }
            val prompt = BiometricPrompt(activity, executor, callback)
            val info =
                BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setNegativeButtonText("Cancel")
                    .build()
            prompt.authenticate(info)
            awaitClose { }
        }
}
