package com.example.screentimetracker.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.screentimetracker.domain.usecases.RecordScreenUnlockUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ScreenUnlockReceiver : BroadcastReceiver() {

    @Inject
    lateinit var recordScreenUnlockUseCase: RecordScreenUnlockUseCase

    // It's better to manage the scope or pass it if possible,
    // but for a BroadcastReceiver, a new scope is often created.
    // Ensure this scope is managed if the receiver has a longer lifecycle or specific needs.
    private val receiverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())


    companion object {
        private const val TAG = "ScreenUnlockReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_USER_PRESENT) {
            Log.d(TAG, "Screen unlocked event received.")

            // GoPendingResult for BroadcastReceivers if the work is async
            // However, Hilt's @AndroidEntryPoint might handle some of this for field injection.
            // For simplicity and if the operation is quick, direct launch is often seen.
            // If DB operation is slow, goAsync() is the proper way.
            val pendingResult: PendingResult? = goAsync() // Important for async work in receivers

            receiverScope.launch {
                try {
                    recordScreenUnlockUseCase() // UseCase handles timestamp internally
                    Log.d(TAG, "Screen unlock event saved via UseCase.")
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving screen unlock event", e)
                } finally {
                    pendingResult?.finish() // Always finish the pending result
                }
            }
        }
    }
}
