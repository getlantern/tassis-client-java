package io.lantern.tassis.websocket

import io.lantern.tassis.Callback
import io.lantern.tassis.MessageHandler
import io.lantern.tassis.Transport
import io.lantern.tassis.TransportFactory
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicReference

/**
 * WSSTransportFactory is a TransportFactory that creates Transports that connect via WebSocket to
 * the given url.
 */
class WebSocketTransportFactory(private val url: String): TransportFactory {
    override fun build(cb: Callback<Transport>) {
        WebSocketTransport(URI(url), cb)
    }
}

internal class WebSocketTransport(serverURI: URI, private val cb: Callback<Transport>): WebSocketClient(serverURI), Transport {
    private val handler = AtomicReference<MessageHandler>()
    private val latestError = AtomicReference<Throwable>()

    override fun setHandler(handler: MessageHandler) {
        this.handler.set(handler)
    }

    override fun onOpen(handshakedata: ServerHandshake?) {
        cb.onSuccess(this)
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        handler.get().onClose(latestError.get())
    }

    override fun onMessage(message: String) {
        // protocol doesn't use text messages
    }

    override fun onMessage(message: ByteBuffer?) {
        this.handler.get().onMessage(message)
    }

    override fun onError(ex: Exception) {
        latestError.set(ex)
    }
}
