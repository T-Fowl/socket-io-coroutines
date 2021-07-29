package com.tfowl.io.socket

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

class SocketConnectErrorException : IOException()
class SocketConnectTimeoutException : IOException()

suspend fun Socket.connectAwait(): Socket {
    if (connected()) return this

    return suspendCancellableCoroutine { cont ->
        once(Socket.EVENT_CONNECT) {
            if (cont.isActive) cont.resume(this)
        }
        once(Socket.EVENT_CONNECT_ERROR) {
            if (cont.isActive) cont.resumeWithException(SocketConnectErrorException())
        }
        once(Socket.EVENT_CONNECT_TIMEOUT) {
            if (cont.isActive) cont.resumeWithException(SocketConnectTimeoutException())
        }

        connect()

        cont.invokeOnCancellation { disconnect() }
    }
}

suspend fun Socket.emitAwait(event: String, vararg args: Any): Array<out Any> =
    suspendCoroutine { cont ->
        emit(event, args) { results ->
            cont.resume(results)
        }
    }

suspend fun Emitter.onceAwait(event: String): Array<out Any> {
    return suspendCancellableCoroutine { cont ->
        val listener: Emitter.Listener = Emitter.Listener { cont.resume(it) }
        cont.invokeOnCancellation { off(event, listener) }
        once(event, listener)
    }
}

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
