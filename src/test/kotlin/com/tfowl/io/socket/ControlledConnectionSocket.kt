package com.tfowl.io.socket

import io.socket.client.Socket

internal enum class ConnectionOutcome {
    Success,
    Error,
    Timeout
}

internal class ControlledConnectionSocket(private val outcome: ConnectionOutcome) :
    Socket(null, null, null) {

    private var connected = false

    override fun connected(): Boolean {
        return connected
    }

    override fun close(): Socket {
        return this
    }

    override fun open(): Socket {
        when (outcome) {
            ConnectionOutcome.Success -> {
                connected = true
                emit(EVENT_CONNECT)
            }
            ConnectionOutcome.Error   -> emit(EVENT_CONNECT_ERROR)
            ConnectionOutcome.Timeout -> emit(EVENT_CONNECT_TIMEOUT)
        }
        return this
    }
}