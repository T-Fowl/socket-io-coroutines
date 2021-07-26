@file:OptIn(ExperimentalCoroutinesApi::class)

package com.tfowl.io.socket

import io.socket.emitter.Emitter
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runBlockingTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private fun Emitter.assertListening(event: String) {
    assertTrue(hasListeners(event))
}

private fun Emitter.assertNotListening(event: String) {
    assertFalse(hasListeners(event))
}

private class EventTestingScope(val event: String, val emitter: Emitter = Emitter())

private inline fun eventTesting(event: String, block: EventTestingScope.() -> Unit) {
    val tester = EventTestingScope(event)
    tester.emitter.assertNotListening(tester.event) // Sanity
    tester.block()
}

class ExtensionsTests {

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

//    @Test
//    @OptIn(ExperimentalTime::class)
//    fun `onFlow removed on cancellations`() = runBlockingTest {
//        eventTesting("onFlow") {
//
//            emitter.onFlow(event)
//
//            emitter.emit(event, 1)
//            emitter.emit(event, 2)
//            emitter.emit(event, 3)
//        }
//    }
}