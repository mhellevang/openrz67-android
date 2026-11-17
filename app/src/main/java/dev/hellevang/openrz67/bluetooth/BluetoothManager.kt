package dev.hellevang.openrz67.bluetooth

import android.util.Log
import com.juul.kable.Filter
import com.juul.kable.Peripheral
import com.juul.kable.Scanner
import com.juul.kable.State
import com.juul.kable.WriteType
import com.juul.kable.characteristicOf
import com.juul.kable.peripheral
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.pow

/**
 * Owns the BLE connection to the openrz67-trigger. The peripheral is bound to [scope];
 * cancelling the scope disconnects it and stops the reconnect loop.
 */
class BluetoothManager(private val scope: CoroutineScope) : BluetoothLink {
    private lateinit var peripheral: Peripheral
    private var setupJob: Job? = null
    private val connectionAttempt = AtomicInteger()

    private val _connectionState = MutableStateFlow(ConnectionState.Idle)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _batteryPercent = MutableStateFlow<Int?>(null)
    override val batteryPercent: StateFlow<Int?> = _batteryPercent.asStateFlow()

    companion object {
        private const val TAG = "BluetoothManager"
        val TARGET_SERVICE_UUID: UUID = UUID.fromString("c9239c9e-6fc9-4168-b3aa-53105eb990b0")
        val TARGET_CHARACTERISTIC_UUID: UUID = UUID.fromString("458d4dc9-349f-401d-b092-a2b1c55f5319")
    }

    private val characteristic = characteristicOf(
        TARGET_SERVICE_UUID.toString(),
        TARGET_CHARACTERISTIC_UUID.toString()
    )

    /** Standard Battery Service, Battery Level: one byte, percent. */
    private val batteryLevel = characteristicOf(
        "0000180f-0000-1000-8000-00805f9b34fb",
        "00002a19-0000-1000-8000-00805f9b34fb"
    )

    override fun connect() {
        connectionAttempt.set(0)
        when {
            ::peripheral.isInitialized ->
                if (_connectionState.value != ConnectionState.Connected) scope.launchConnect()
            setupJob?.isActive != true -> setupJob = scope.launch { scanAndConnect() }
        }
    }

    private suspend fun scanAndConnect() {
        try {
            _connectionState.value = ConnectionState.Scanning
            val advertisement = Scanner {
                filters = listOf(Filter.Service(TARGET_SERVICE_UUID))
            }.advertisements.first()

            _connectionState.value = ConnectionState.Connecting
            peripheral = scope.peripheral(advertisement)

            scope.enableAutoReconnect()
            scope.launchConnect()

            peripheral.state.collectLatest { state ->
                _connectionState.value = when (state) {
                    is State.Connected -> ConnectionState.Connected
                    is State.Connecting -> ConnectionState.Connecting
                    is State.Disconnecting, is State.Disconnected -> ConnectionState.Disconnected
                }
                if (state is State.Connected) trackBattery() else _batteryPercent.value = null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Failed to initialize Bluetooth", e)
            _connectionState.value = ConnectionState.Failed
        }
    }

    private suspend fun trackBattery() {
        try {
            _batteryPercent.value = peripheral.read(batteryLevel).firstOrNull()?.toInt()
            peripheral.observe(batteryLevel).collect { _batteryPercent.value = it.firstOrNull()?.toInt() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.d(TAG, "No battery level: ${e.message}")
        }
    }

    private fun CoroutineScope.enableAutoReconnect() {
        peripheral.state
            .filter { it is State.Disconnected }
            .onEach {
                val attempt = connectionAttempt.getAndIncrement()
                val waitMillis = minOf((500L * 2f.pow(attempt)).toLong(), 30_000L)
                Log.d(TAG, "Reconnecting in $waitMillis ms (attempt ${attempt + 1})")
                delay(waitMillis)
                launchConnect()
            }
            .launchIn(this)
    }

    private fun CoroutineScope.launchConnect() {
        launch {
            try {
                peripheral.connect()
                connectionAttempt.set(0)
            } catch (e: Exception) {
                Log.w(TAG, "Connection attempt failed: ${e.message}")
            }
        }
    }

    // WithResponse: the write only returns once the trigger has accepted the command.
    override suspend fun sendCountdown(durationSeconds: Int, start: Boolean) {
        val action: Byte = if (start) 1 else 0
        peripheral.write(characteristic, byteArrayOf(3, durationSeconds.toByte(), action), WriteType.WithResponse)
    }

    override suspend fun sendSignal(signalType: SignalType, on: Boolean) {
        val code = signalType.base + if (on) 1 else 0
        peripheral.write(characteristic, byteArrayOf(code.toByte()), WriteType.WithResponse)
    }
}
