package dev.hellevang.openrz67.viewmodel

import dev.hellevang.openrz67.bluetooth.BluetoothLink
import dev.hellevang.openrz67.bluetooth.ConnectionState
import dev.hellevang.openrz67.bluetooth.SignalType
import dev.hellevang.openrz67.viewmodel.TriggerControlViewModel.TriggerType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class TriggerControlViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val link = FakeLink()
    private lateinit var vm: TriggerControlViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        vm = TriggerControlViewModel { link }
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `direct mode sends trigger press`() = runTest(dispatcher) {
        vm.handleTriggerButtonClick()
        runCurrent()
        assertEquals(listOf("Trigger on"), link.writes)
    }

    @Test
    fun `countdown runs locally after successful write`() = runTest(dispatcher) {
        vm.setTriggerType(TriggerType.Countdown)
        vm.handleTriggerButtonClick()
        runCurrent()
        assertEquals(listOf("countdown 10 start"), link.writes)
        assertTrue(vm.startDelayedTrigger.value)
        assertEquals(10, vm.countdownTimeLeft.value)

        advanceTimeBy(10_001)
        assertFalse(vm.startDelayedTrigger.value)
        assertEquals(0, vm.countdownTimeLeft.value)
    }

    @Test
    fun `failed write does not start countdown`() = runTest(dispatcher) {
        link.fail = true
        vm.setTriggerType(TriggerType.Countdown)
        vm.handleTriggerButtonClick()
        runCurrent()
        assertFalse(vm.startDelayedTrigger.value)
        assertEquals(0, vm.countdownTimeLeft.value)
    }

    @Test
    fun `leaving countdown mode stops a running countdown`() = runTest(dispatcher) {
        vm.setTriggerType(TriggerType.Countdown)
        vm.handleTriggerButtonClick()
        runCurrent()

        vm.setTriggerType(TriggerType.Bulb)
        runCurrent()
        assertEquals(TriggerType.Bulb, vm.triggerType.value)
        assertFalse(vm.startDelayedTrigger.value)
        assertEquals("countdown 10 stop", link.writes.last())
    }

    @Test
    fun `bulb toggles and is released when leaving bulb mode`() = runTest(dispatcher) {
        vm.setTriggerType(TriggerType.Bulb)
        vm.handleTriggerButtonClick()
        runCurrent()
        assertTrue(vm.isBulbActive.value)
        assertEquals("BulbMode on", link.writes.last())
        advanceTimeBy(3_001)
        assertEquals(3, vm.bulbElapsedSeconds.value)

        vm.setTriggerType(TriggerType.Direct)
        runCurrent()
        assertEquals(TriggerType.Direct, vm.triggerType.value)
        assertFalse(vm.isBulbActive.value)
        assertEquals(0, vm.bulbElapsedSeconds.value)
        assertEquals("BulbMode off", link.writes.last())
    }

    @Test
    fun `failed bulb write leaves state unchanged`() = runTest(dispatcher) {
        link.fail = true
        vm.setTriggerType(TriggerType.Bulb)
        vm.handleTriggerButtonClick()
        runCurrent()
        assertFalse(vm.isBulbActive.value)
    }

    @Test
    fun `disconnect clears bulb, since the trigger closes the shutter itself`() = runTest(dispatcher) {
        vm.setTriggerType(TriggerType.Bulb)
        vm.handleTriggerButtonClick()
        runCurrent()
        assertTrue(vm.isBulbActive.value)

        link.connectionState.value = ConnectionState.Disconnected
        runCurrent()
        assertFalse(vm.isBulbActive.value)
        assertEquals(0, vm.bulbElapsedSeconds.value)
    }

    @Test
    fun `mode does not change when the stop fails`() = runTest(dispatcher) {
        vm.setTriggerType(TriggerType.Bulb)
        vm.handleTriggerButtonClick()
        runCurrent()

        link.fail = true
        vm.setTriggerType(TriggerType.Direct)
        runCurrent()
        assertEquals(TriggerType.Bulb, vm.triggerType.value)
        assertTrue(vm.isBulbActive.value)

        // runTest never finishes while the elapsed-time ticker is alive
        link.fail = false
        vm.handleTriggerButtonClick()
        runCurrent()
    }

    @Test
    fun `countdown keeps running when the stop fails`() = runTest(dispatcher) {
        vm.setTriggerType(TriggerType.Countdown)
        vm.handleTriggerButtonClick()
        runCurrent()

        link.fail = true
        vm.handleTriggerButtonClick()
        runCurrent()
        assertTrue(vm.startDelayedTrigger.value)
    }

    @Test
    fun `duration must fit in one byte`() {
        vm.setCountdownDuration(0)
        assertEquals(10, vm.countdownDuration.value)
        vm.setCountdownDuration(256)
        assertEquals(10, vm.countdownDuration.value)
        vm.setCountdownDuration(255)
        assertEquals(255, vm.countdownDuration.value)
    }

    private class FakeLink : BluetoothLink {
        override val connectionState = MutableStateFlow(ConnectionState.Connected)
        override val batteryPercent = MutableStateFlow<Int?>(null)
        val writes = mutableListOf<String>()
        var fail = false

        override fun connect() = Unit

        override suspend fun sendCountdown(durationSeconds: Int, start: Boolean) =
            write("countdown $durationSeconds ${if (start) "start" else "stop"}")

        override suspend fun sendSignal(signalType: SignalType, on: Boolean) =
            write("$signalType ${if (on) "on" else "off"}")

        private fun write(entry: String) {
            if (fail) throw IOException("fake write failure")
            writes += entry
        }
    }
}
