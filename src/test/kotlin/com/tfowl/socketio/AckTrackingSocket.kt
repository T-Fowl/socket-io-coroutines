package com.tfowl.socketio

import io.socket.client.Ack
import io.socket.client.Socket
import io.socket.emitter.Emitter

internal class AckTrackingSocket : Socket(null, null, null) {

    private var latestAck: Ack? = null

    fun respondLatestAck(vararg results: Any) {
        latestAck?.call(*results)
    }

    override fun emit(event: String, args: Array<out Any>, ack: Ack): Emitter {
        latestAck = ack
        return this
    }
}