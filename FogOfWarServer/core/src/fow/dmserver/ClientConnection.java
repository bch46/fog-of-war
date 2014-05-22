package fow.dmserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketTimeoutException;

import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.GdxRuntimeException;

import fow.common.NetworkEvent;
import fow.common.NetworkEvent.Type;

/**
 * ClientConnection handles the socket connection with a remote client by relaying messages to the
 * associated server and allowing the server to send messages as needed.
 */
public class ClientConnection {

    private Server server;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    private boolean alive;
    private boolean idle;
    private boolean confirmed;
    private int id;

    private boolean isDm;

    private boolean debug;

    /**
     * Initializes the in and out streams from the given socket. Starts a thread to listen to
     * communication from the client.
     * 
     * @param server The server associated with this ClientConnection.
     * @param socket The socket this ClientConnection reads from and writes to.
     */
    public ClientConnection(final Server server, final Socket socket, final int id) {
        this.id = id;
        this.server = server;
        this.socket = socket;
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            alive = false;
            return;
        }
        alive = true;
        isDm = false;
        startConnectionListener();
    }

    /**
     * See other constructor.
     * 
     * @param debug If we should print information to the console about sent events.
     * @author Jeran
     */
    public ClientConnection(final Server server, final Socket socket, final int id,
            final boolean debug) {
        this(server, socket, id);
        this.debug = debug;
    }

    /**
     * Creates and runs the thread used to listen to the socket and add events to the server's event
     * queue. When kill() is called, this thread will quit it's loop and close the socket. Assumes
     * sendEvent() and kill() will only be called from a single thread, and sendEvent() will not be
     * called after kill().
     * 
     * @author Jeran
     */
    public void startConnectionListener() {
        new Thread() {
            @Override
            public void run() {
                while (alive) {
                    try {
                        NetworkEvent event = (NetworkEvent) in.readObject();
                        server.enqueueEvent(event);
                        idle = false;
                        if (debug) System.out.println("server enqueued event: " + event);
                    } catch (ClassNotFoundException e) {
                        // TODO
                        e.printStackTrace();
                    } catch (final SocketTimeoutException e) {
                        handleIdleTimeOut();
                    } catch (final IOException e) {
                        kill();
                    }
                }
                try {
                    in.close();
                    out.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                try {
                    socket.dispose();
                } catch (final GdxRuntimeException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * Responds to an SOTimeout from the socket by informing the server. If it's the first time the
     * server will ping the client. If it's the second time, the server will forget about the
     * client.
     * 
     * @author Jeran
     */
    public void handleIdleTimeOut() {
        if (idle) {
            kill();
        } else {
            idle = true;
            server.enqueueEvent(new NetworkEvent(Type.IDLE, new Object[] {confirmed, id}));
        }
    }

    /**
     * Sends an event to the client.
     * 
     * @param event The event to send.
     * @author Jeran
     */
    public void sendEvent(final NetworkEvent event) {
        try {
            if (debug) System.out.println("server sending event: " + event);
            // Reset or else new versions of the same object get read from cache and are out of date
            out.reset();
            out.writeObject(event);
            out.flush();
        } catch (final IOException e) {
            kill();
        }
    }

    /**
     * Ends the loop in the connection listener thread, letting it close all connections afterwards.
     * 
     * @author Jeran
     */
    public void kill() {
        if (alive) {
            alive = false;
            idle = true;
            server.enqueueEvent(new NetworkEvent(Type.DISCONNECT, new Object[] {confirmed, id}));
            try {
                if (debug) System.out.println("server sending kill event");
                out.reset();
                out.writeObject(new NetworkEvent(Type.DISCONNECT, null));
                out.flush();
            } catch (final IOException e) {
                // Normally we'd call kill(), so here we obviously don't care
            }
        }
    }

    /**
     * @param confirmed If the client has completed the handshake
     */
    public void setConfirmed(final boolean confirmed) {
        this.confirmed = confirmed;
    }

    /**
     * On creation, the ClientConnection is given a temporary id. On completion of the handshake,
     * the ClientConnection stores its client's accountId.
     * 
     * @param accId The accoundId of the client
     */
    public void setId(final int accId) {
        id = accId;
    }

    public int getId() {
        return id;
    }

    public void setDm(final boolean isDm) {
        this.isDm = isDm;
    }

    /**
     * @return
     */
    public boolean isDm() {
        return isDm;
    }
}
