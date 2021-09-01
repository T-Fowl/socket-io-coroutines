package com.tfowl.socketio

import io.socket.emitter.Emitter
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

internal fun Emitter.assertListening(event: String) {
    assertTrue(hasListeners(event))
}

internal fun Emitter.assertNotListening(event: String) {
    assertFalse(hasListeners(event))
}

internal class EventTestingScope(val event: String, val emitter: Emitter = Emitter())

internal inline fun eventTesting(event: String, block: EventTestingScope.() -> Unit) {
    val tester = EventTestingScope(event)
    tester.emitter.assertNotListening(tester.event) // Sanity
    tester.block()
}