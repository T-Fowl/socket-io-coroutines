package com.tfowl.socketio

import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class SocketIOConnectionException : IOException()

/**
 * Calls [Socket.connect] and then
 * suspends the current coroutine until one of [Socket.EVENT_CONNECT]
 * or [Socket.EVENT_CONNECT_ERROR] is emitted.
 *
 * Invokes [Socket.disconnect] on cancellation
 */
suspend fun Socket.connectAwait(): Socket {
    if (connected()) return this

    return suspendCancellableCoroutine { cont ->
        once(Socket.EVENT_CONNECT) {
            if (cont.isActive) cont.resume(this)
        }
        once(Socket.EVENT_CONNECT_ERROR) {
            if (cont.isActive) cont.resumeWithException(SocketIOConnectionException())
        }

        connect()

        cont.invokeOnCancellation { disconnect() }
    }
}

/**
 * Calls [Socket.emit] and then suspends until [io.socket.client.Ack.call] is invoked.
 */
suspend fun Socket.emitAwait(event: String, vararg args: Any): Array<out Any> =
    suspendCoroutine { cont ->
        emit(event, args) { results ->
            cont.resume(results)
        }
    }

/**
 * Installs a single-use [Emitter.Listener] and suspends the current
 * coroutine until it is invoked. Removed the listener on cancellation.
 */
suspend fun Emitter.onceAwait(event: String): Array<out Any> {
    return suspendCancellableCoroutine { cont ->
        val listener: Emitter.Listener = Emitter.Listener {
            if (cont.isActive)
                cont.resume(it)
        }
        cont.invokeOnCancellation { off(event, listener) }
        once(event, listener)
    }
}

/**
 * Registers a [Emitter.Listener] which will send incoming events
 * to a channel-backed [Flow]. If an event fails to be sent to
 * the flow then the listener is removed.
 * Closing the flow also removes the listener.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("BlockingMethodInNonBlockingContext")
fun Emitter.onFlow(event: String): Flow<Array<out Any>> = callbackFlow {
    val listener = object : Emitter.Listener {
        override fun call(vararg args: Any) {
            trySendBlocking(args).onFailure {
                off(event, this)
            }
        }
    }

    on(event, listener)

    awaitClose { off(event, listener) }
}
