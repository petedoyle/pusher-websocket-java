package com.pusher.client.connection.websocket;

import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

/**
 * A thin wrapper around the WebSocketClient class from the Java-WebSocket
 * library. The purpose of this class is to enable the WebSocketConnection class
 * to be unit tested by swapping out an instance of this wrapper for a mock
 * version.
 */
public class WebSocketClientWrapper extends WebSocketClient {

    private static final String WSS_SCHEME = "wss";
    private final WebSocketListener proxy;

    public WebSocketClientWrapper(final URI uri, final WebSocketListener proxy, int connectTimeout) throws SSLException {
        super(uri, new Draft_17(), null, connectTimeout);

        if (uri.getScheme().equals(WSS_SCHEME)) {
            try {
                SSLContext sslContext = null;
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, null, null); // will use java's default
                                                   // key and trust store which
                                                   // is sufficient unless you
                                                   // deal with self-signed
                                                   // certificates

                final SSLSocketFactory factory = sslContext.getSocketFactory();// (SSLSocketFactory)
                                                                               // SSLSocketFactory.getDefault();

                setSocket(factory.createSocket());
            }
            catch (final IOException e) {
                throw new SSLException(e);
            }
            catch (final NoSuchAlgorithmException e) {
                throw new SSLException(e);
            }
            catch (final KeyManagementException e) {
                throw new SSLException(e);
            }
        }

        this.proxy = proxy;
    }

    @Override
    public void onOpen(final ServerHandshake handshakedata) {
        proxy.onOpen(handshakedata);
    }

    @Override
    public void onMessage(final String message) {
        proxy.onMessage(message);
    }

    @Override
    public void onClose(final int code, final String reason, final boolean remote) {
        proxy.onClose(code, reason, remote);
    }

    @Override
    public void onError(final Exception ex) {
        proxy.onError(ex);
    }
}
