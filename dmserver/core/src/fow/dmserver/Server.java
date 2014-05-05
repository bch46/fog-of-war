package fow.dmserver;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.GdxRuntimeException;

import fow.common.NetworkEvent;
import fow.common.NetworkEvent.Type;

/**
 * A generic Server class for event based server communication. Handles addition of new clients
 * connections and timeout of client connections. To use, extend and override handleEvent().
 * Original architecture by Jeran and Ben.
 * 
 */
public class Server extends Thread {

    /** The length of time a client has to respond before the server cuts connection */
    private final static int TIMEOUT = 20000;

    private ServerSocket serverSocket;
    protected final HashMap<Short, ClientConnection> unconfirmedClientConnections =
            new HashMap<Short, ClientConnection>();
    protected final HashMap<Short, ClientConnection> confirmedClientConnections =
            new HashMap<Short, ClientConnection>();
    /** A map from account id to game id */
    protected HashMap<Integer, Short> gameIds = new HashMap<Integer, Short>();

    private LinkedBlockingQueue<NetworkEvent> eventQueue = new LinkedBlockingQueue<NetworkEvent>();

    private NetworkEventHandler eventHandler;
    
    private boolean alive;

    private boolean debug;

    /**
     * Starts a ServerSocket at the given port for incoming client connections.
     * 
     * @param connectionListenerPort The port for the ServerSocket that listens for incoming
     *        connections from clients
     */
    public Server(final int connectionListenerPort) {
        try {
            serverSocket = Gdx.net.newServerSocket(Protocol.TCP, connectionListenerPort, null);
        } catch (final GdxRuntimeException e) {
            e.printStackTrace();
        }
        eventHandler = new NetworkEventHandler(this);
    }

    /**
     * Starts a ServerSocket at the given port for incoming client connections.
     * 
     * @param connectionListenerPort The port for the ServerSocket that listens for incoming
     *        connections from clients
     * @param debug If we should print information to the console about sent events.
     */
    public Server(final int connectionListenerPort, final boolean debug) {
        this(connectionListenerPort);
        this.debug = debug;
    }

    /**
     * Creates and runs the thread used to listen for incoming connections and creating
     * corresponding ClientConnections.
     */
    private void startConnectionListener() {
        new Thread() {
            @Override
            public void run() {
                while (alive) {
                    try {
                        SocketHints hints = new SocketHints();
                        hints.socketTimeout = TIMEOUT;
                        final Socket clientSocket = serverSocket.accept(hints);
                        enqueueEvent(new NetworkEvent(Type.NEW_CONNECTION, clientSocket));
                    } catch (final GdxRuntimeException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    /**
     * Starts a thread for listening for incoming client connections and starts a consumer loop for
     * handling events.
     */
    @Override
    public void run() {
        System.out.println(Util.findIp());
        alive = true;
        startConnectionListener();
        while (alive) {
            try {
                final NetworkEvent event = eventQueue.take();
                if (debug) System.out.println("server consumed event: " + event);
                eventHandler.handleEvent(event);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Confirms a client. Protected so subclasses can respond to confirmations.
     * 
     * @param clientConnection The client connection to confirm
     * @param gameId The generated gameId for this client
     */
    protected void confirmClient(final ClientConnection clientConnection, final short gameId) {
    	clientConnection.setConfirmed(true);
        clientConnection.setId(gameId);
        confirmedClientConnections.put(gameId, clientConnection);
        clientConnection.sendEvent(new NetworkEvent(Type.CONFIRMED, gameId));
    }

    /**
     * Adds an event to the event queue.
     * 
     * @param event The event to add
     */
    public void enqueueEvent(final NetworkEvent event) {
        eventQueue.add(event);
    }

    /**
     * Kill the main event handling thread and connection listener thread.
     */
    public void kill() {
    	// TODO see if order should be switched, killing serverSocket might trigger exceptions
    	// in client sockets
        alive = false;
        serverSocket.dispose();
        for (ClientConnection client : confirmedClientConnections.values()) {
            client.kill();
        }
        for (ClientConnection client : unconfirmedClientConnections.values()) {
            client.kill();
        }
    }
}
