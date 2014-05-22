package fow.app.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.GdxRuntimeException;

import fow.common.NetworkEvent;
import fow.common.NetworkEvent.Type;

/**
 * Class to manage a client-side connection to the server. Original code by Jeran and Ben
 * 
 */
public class ServerConnection {
    private LinkedBlockingQueue<NetworkEvent> inEventQueue =
            new LinkedBlockingQueue<NetworkEvent>();
    private LinkedBlockingQueue<NetworkEvent> outEventQueue =
            new LinkedBlockingQueue<NetworkEvent>();

    private OnReceiveNetworkEventListener onReceiveNetworkEventListener;

    /** The lock for setting the onReceiveNetworkEventListener */
    private final Lock onReceiveLock = new ReentrantLock();

    private Socket socket;

    private ObjectInputStream in;
    private ObjectOutputStream out;

    private int accountId;

    private boolean isDm;

    private boolean alive;

    private String ip;
    private int port;

    private boolean debug;

    /**
     * Sets all data needed for a connection.
     * 
     * @param ip The ip address
     * @param port The port number
     * @param accountId The accountId of the player
     * @param onDisconnectListener The listener that gets called when a disconnect happens, if null
     *        nothing is called
     * @param debug If we should print information to the console about sent events.
     */
    public ServerConnection(final String ip, final int port, final int accountId,
            final boolean debug) {
        this.ip = ip;
        this.port = port;
        this.accountId = accountId;
        this.debug = debug;
    }

    /**
     * Sets all data needed for a connection.
     * 
     * @param ip The ip address
     * @param port The port number
     * @param accountId The accountId of the player
     * @param onDisconnectListener The listener that gets called when a disconnect happens, if null
     *        nothing is called
     */
    public ServerConnection(final String ip, final int port, final int accountId) {
        this(ip, port, accountId, false);
    }

    /**
     * Attempts to open the socket and connect to the server. Returns false if it fails to open the
     * socket. If successful, starts all threads for sending, receiving, and handling events.
     * 
     * Warning: This method will provide no feedback on whether or not the connection was
     * successful. To check if the connection was successful you might call isAlive at a later
     * point.
     */
    public boolean connect() {
        return connect(new HandshakeListener());
    }

    /**
     * Attempts to open the socket and connect to the server. Returns false if it fails to open the
     * socket. If successful, starts all threads for sending, receiving, and handling events,
     * setting handshakeListener as the current onReceiveNetworkEventListener.
     * 
     * Use the handshakeListener's OnHandshakeResultListener to detect whether or not the handshake
     * was successful. If it was unsuccessful, the ServerConnection will be automatically killed.
     * 
     * @return true if socket was opened successfully, false if not.
     */
    public boolean connect(final HandshakeListener handshakeListener) {
        if (openSocket()) {
            alive = true;
            setOnReceiveNetworkEventListener(handshakeListener);
            startInProducerThread();
            startOutConsumerThread();
        } else {
            alive = false;
        }
        return alive;
    }

    /**
     * Attempts to open the socket and returns false on failure.
     * 
     * @return If opening the socket was successful.
     */
    private boolean openSocket() {
        try {
            Socket socket = Gdx.net.newClientSocket(Protocol.TCP, ip, port, null);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
        } catch (final IOException e) {
            e.printStackTrace();
            return false;
        } catch (final GdxRuntimeException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Starts the thread that listens for incoming events from the server and adds them to the
     * inQueue.
     */
    private void startInProducerThread() {
        new Thread() {
            @Override
            public void run() {
                while (alive) {
                    try {
                        final NetworkEvent event = (NetworkEvent) in.readObject();
                        inEventQueue.add(event);
                        if (debug) System.out.println("client enqueued event: " + event);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        connectionFailed();
                    } catch (IOException e) {
                        e.printStackTrace();
                        connectionFailed();
                    }
                }
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    socket.dispose();
                } catch (GdxRuntimeException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * Starts the thread that consumes events from the outQueue and sends them to the server.
     */
    private void startOutConsumerThread() {
        new Thread() {
            @Override
            public void run() {
                while (alive) {
                    try {
                        final NetworkEvent event = outEventQueue.take();
                        // Reset or else new versions of the same object get read from cache and are
                        // out of date
                        out.reset();
                        out.writeObject(event);
                        out.flush();
                        if (debug) System.out.println("client sent event: " + event);
                    } catch (final IOException e) {
                        e.printStackTrace();
                        connectionFailed();
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    socket.dispose();
                } catch (GdxRuntimeException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * Kills the connection if it hasn't been already. Runs the onDisconnectListener.
     */
    private void connectionFailed() {
        if (alive) {
            new Thread() {
                @Override
                public void run() {
                    inEventQueue.add(new NetworkEvent(Type.FAILED_CONNECTION, null));
                    inEventQueue.add(new NetworkEvent(Type.DISCONNECT, null));
                }
            }.start();
        }
    }

    /**
     * Sets alive to false killing all threads.
     */
    public void kill() {
        alive = false;
    }

    /**
     * Checks if the ServerConnection is currently alive and running.
     * 
     * @return true if alive, false otherwise
     */
    public boolean isAlive() {
        return alive;
    }

    /**
     * If the onReceiveNetworkEventListener is set to null and an event comes in, it will be taken
     * off the queue, store it here for a new listener to handle once it gets set. Consider it a
     * ramp up task for the newbie listener.
     */
    private NetworkEvent oops;

    /**
     * Starts the thread for consuming events from the inQueue. Started separately from other
     * threads because this can be turned on and off by setting a listener.
     */
    private void startInEventConsumerThread() {
        new Thread() {
            @Override
            public void run() {
                while (alive) {
                    try {
                        if (oops != null) {
                            onReceiveLock.lock();
                            try {
                                if (onReceiveNetworkEventListener == null) {
                                    break;
                                } else {
                                    onReceiveNetworkEventListener.onReceiveNetworkEvent(
                                            ServerConnection.this, oops);
                                    oops = null;
                                }
                            } finally {
                                onReceiveLock.unlock();
                            }
                        }

                        final NetworkEvent event = inEventQueue.take();
                        onReceiveLock.lock();
                        try {
                            if (onReceiveNetworkEventListener == null) {
                                oops = event;
                                break;
                            } else {
                                onReceiveNetworkEventListener.onReceiveNetworkEvent(
                                        ServerConnection.this, event);
                            }
                        } finally {
                            onReceiveLock.unlock();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    /**
     * Put an event to send on the outQueue.
     * 
     * @param event The event to send
     */
    public void sendEvent(final NetworkEvent event) {
        event.setAccountId(accountId);
        outEventQueue.add(event);
    }

    /**
     * Enqueue an event on the inQueue. This is used to post events from the client-side that you
     * want the ServerConnection to handle, like a disconnect event from the OnDisconnectListener.
     * 
     * @param event The event to enqueue
     */
    public void enqueueEvent(final NetworkEvent event) {
        inEventQueue.add(event);
    }

    /**
     * Set the listener to respond to received network events. If no listener is set, inQueue can
     * continue to fill and will be processed when the listener is next set.
     * 
     * @param newListener
     */
    public void setOnReceiveNetworkEventListener(final OnReceiveNetworkEventListener newListener) {
        boolean needsStarting = false;
        onReceiveLock.lock();
        try {
            if (onReceiveNetworkEventListener != null) { // consume is running,
                                                         // just switch
                onReceiveNetworkEventListener = newListener;
            } else { // consume thread wasn't running because listener was null
                onReceiveNetworkEventListener = newListener;
                needsStarting = true;
            }
        } finally {
            onReceiveLock.unlock();
        }
        if (needsStarting) {
            startInEventConsumerThread();
        }
    }

    public int getAccountId() {
        return accountId;
    }

    public boolean isDm() {
        return isDm;
    }

    public void setDm(boolean isDm) {
        this.isDm = isDm;
    }

    public interface OnReceiveNetworkEventListener {
        public void onReceiveNetworkEvent(ServerConnection connection, NetworkEvent event);
    }
}
