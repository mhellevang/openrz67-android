package dev.hellevang.openrz67.bluetooth

import kotlinx.coroutines.flow.StateFlow

/** Firmware command codes: base value = release/off, base + 1 = press/on. */
enum class SignalType(val base: Int) {
    Trigger(10),
    BulbMode(20)
}

enum class ConnectionState {
    Idle,
    Scanning,
    Connecting,
    Connected,
    Disconnected,
    Failed
}

/** What the ViewModel needs from the trigger connection. Implemented by [BluetoothManager]. */
interface BluetoothLink {
    val connectionState: StateFlow<ConnectionState>

    /** Null while unknown: not connected, or a trigger without the Battery Service. */
    val batteryPercent: StateFlow<Int?>

    /** Starts scanning, or retries the connection. Safe to call in any state. */
    fun connect()

    /** Writes throw on failure; callers decide how to surface that. */
    suspend fun sendCountdown(durationSeconds: Int, start: Boolean)
    suspend fun sendSignal(signalType: SignalType, on: Boolean = true)
}
