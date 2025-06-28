package com.example.screentimetracker.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.example.screentimetracker.data.local.ScreenUnlockEvent
import com.example.screentimetracker.domain.repository.TrackerRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ScreenUnlockReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: TrackerRepository

    companion object {
        fun register(context: Context) {
            val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
            context.registerReceiver(ScreenUnlockReceiver(), filter)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_USER_PRESENT) {
            Log.d("ScreenUnlockReceiver", "Screen unlocked detected!")
            val timestamp = System.currentTimeMillis()
            val event = ScreenUnlockEvent(timestamp = timestamp)

            // Use a CoroutineScope to insert into the database as onReceive is on main thread
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    repository.insertScreenUnlockEvent(event)
                    Log.d("ScreenUnlockReceiver", "Screen unlock event inserted: $event")
                } catch (e: Exception) {
                    Log.e("ScreenUnlockReceiver", "Error inserting screen unlock event", e)
                }
            }
        }
    }
}