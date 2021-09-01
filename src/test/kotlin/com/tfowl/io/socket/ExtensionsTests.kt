@file:OptIn(ExperimentalCoroutinesApi::class)

package com.tfowl.io.socket

import app.cash.turbine.test
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.time.ExperimentalTime

class ExtensionsTests {

    @Test
    fun `connectAwait returns correctly`(): Unit = runBlocking {
        val socketSuccessful = ControlledConnectionSocket(ConnectionOutcome.Success)

        val connected = socketSuccessful.connectAwait()
        assertTrue(connected.connected())

        val socketError = ControlledConnectionSocket(ConnectionOutcome.Error)
        assertThrows<SocketConnectErrorException> { socketError.connectAwait() }

        val socketTimeout = ControlledConnectionSocket(ConnectionOutcome.Timeout)
        assertThrows<SocketConnectTimeoutException> { socketTimeout.connectAwait() }
    }

    @Test
    fun `onceAwait returns correctly`() = runBlockingTest {
        eventTesting("onceAwait") {
            val result = async { emitter.onceAwait(event) }

            emitter.assertListening(event)

            emitter.emit(event, 42)

            emitter.assertNotListening(event)

            assertEquals(42, result.await().first())
        }
    }

    @Test
    @Suppress("UNUSED_VARIABLE")
    fun `onceAwait cancels correctly`() = runBlockingTest {
        eventTesting("onceAwaitCancelled") {
            val scope = CoroutineScope(coroutineContext + Job())
            val ignored = scope.async { emitter.onceAwait(event) }

            emitter.assertListening(event)

            scope.cancel()

            emitter.assertNotListening(event)
        }
    }

    @Test
    fun `emitAwait returns correctly`() = runBlockingTest {
        val socket = AckTrackingSocket()

        val result = async { socket.emitAwait("emitAwait", 42) }

        socket.respondLatestAck(84)

        assertEquals(84, result.await().first())
    }

    @Test
    @OptIn(ExperimentalTime::class)
    fun `onFlow receives events`() = runBlockingTest {
        eventTesting("onFlow") {

            launch {
                emitter.emit(event, 1)
                emitter.emit(event, 2)
                emitter.emit(event, 3)
            }
            launch {
                emitter.onFlow(event).test {
                    assertEquals(1, expectItem().first())
                    assertEquals(2, expectItem().first())
                    assertEquals(3, expectItem().first())
                    cancel()
                }
            }
        }
    }

    @Test
    @OptIn(ExperimentalTime::class)
    fun `onFlow registers and removes listeners`() = runBlockingTest {
        eventTesting("onFlow") {

            emitter.onFlow(event).test {
                emitter.assertListening(event)

                cancel()
            }

            emitter.assertNotListening(event)
        }
    }
}