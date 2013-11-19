/*
 */
package DistributedSystems;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * DSConnection This class implements a connection according to the DS assignment 1. Methods: empty constructor, connect, disconnect, send, receive Throwing all appropriate
 * exceptions to be handled in the UI classes.
 *
 * Optional: State ToString(), sent/received byte count for current session. TODO: stream closing after throwing exceptions (to be handled by UI layer) TODO: Possibly add relevant
 * logging?
 *
 * @author Maximilian
 */
public class DSConnection {

    //null if disconnected/closed.
    private Socket m_Socket;
    private long m_bytes_sent = 0, m_bytes_received = 0;

    /**
     * Empty constructor.
     * <p>
     * Does not initialize internal socket as it will be created by connect method.
     */
    public DSConnection() {
    }

    /**
     * Attempts to establish a socket connection to a specified host.
     * <p>
     * On establishing a connection also receives the initial welcome message from the server.
     *
     * @param host String that can be parsed into a remote host (ip or url)
     * @param port Port number of remote host. (Possibly add sanity checks?)
     * @param timeout timeout to set to Socket after establishing contact.
     * @throws UnknownHostException Invalid host/port provided.
     * @throws IOException If connection already established, connection could not be established in timeout (possibly add retries?) or failure during receiving welcome message.
     */
    public void connect(String host, int port, int timeout) throws UnknownHostException, IOException {
        if (m_Socket != null && m_Socket.isConnected()) {
            throw new IOException("Already established a connection, disconnect first.");
        }
        if (m_Socket == null) {
            //Attempt to parse host string
            InetAddress remote_host = InetAddress.getByName(host);
            m_Socket = new Socket(remote_host, port);
            m_Socket.setSoTimeout(timeout);

            m_bytes_sent = 0;
            m_bytes_received = 0;

            //Server send message on connection, so receive it...
            //byte[] server_welcome_message = this.receive();
            //Possibly verify against expected value?
            //System.out.print(new String(server_welcome_message));
        }
    }

    /**
     * Disconnects established connection or throws IOException if none established.
     *
     * @throws IOException No closable connection established or error during closure procedure.
     */
    public void disconnect() throws IOException {
        if (m_Socket == null || m_Socket.isClosed() || !m_Socket.isConnected()) {
            throw new IOException("No connection established.");
        } else {
            m_Socket.close();
            m_Socket = null;
        }
    }

    /**
     * Sends an array of bytes to the remote host.
     * <p>
     * Throws IOException if no connection established or failure to obtain and use the Streams. Terminates the data with the required '\n' delimiter, flushes and closes the
     * stream.
     *
     * @param data Byte array of data to send. (Possibly add length sanity checks?)
     * @throws IOException No valid connection established or error during sending procedure.
     */
    private static final int Server_MaxLength = 128 * 1024; //128 kByte according to assignment pdf

    public void send(byte[] data) throws IOException {
        if (m_Socket == null || m_Socket.isClosed() || !m_Socket.isConnected()) {
            throw new IOException("No connection established.");
        } else {
            OutputStream s_out = m_Socket.getOutputStream();
            if (data.length > Server_MaxLength) {
                System.out.println("Message length exceeds server buffer, proceeding after trimming.");
                byte[] trimdata = new byte[Server_MaxLength];
                System.arraycopy(data, 0, trimdata, 0, trimdata.length);
                data = trimdata;
            }

            s_out.write(data);
            s_out.write((byte) 13);
            s_out.flush();
            m_bytes_sent += data.length + 1; //counts trimmed
        }
    }

    /**
     * Receives a message over an established connection.
     * <p>
     * Throws IOException if no connection established or failure to obtain and use the Streams. Terminates after reading the '\n' delimiter, flushes and closes the stream.
     *
     * @return The received array of bytes, not including the delimiter.
     * @throws IOException No valid connection established error or during receiving procedure.
     */
    public byte[] receive() throws IOException {
        if (m_Socket == null || m_Socket.isClosed() || !m_Socket.isConnected()) {
            throw new IOException("No connection established.");
        }
        InputStream s_in = m_Socket.getInputStream();
        ArrayList<Byte> messagebuffer = new ArrayList<Byte>();

        int val;
        while ((val = s_in.read()) != -1) { //-1 : end of stream (possibly network issue?)

            if (val == 13) { //= '\n', message delimiter
                break;
            } else {
                messagebuffer.add((byte) val);
            }
        }
        byte[] result = new byte[messagebuffer.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) messagebuffer.get(i);
        }
        this.m_bytes_received += result.length;
        return result;
    }

    /**
     * Text description of this DSConnection.
     * <p>
     * For established connections also reveals local ip/port and remote ip/port as well as number of sent and received bytes.
     *
     * @return The description
     */
    public String ToString() {
        if (m_Socket == null || m_Socket.isClosed() || !m_Socket.isConnected()) {
            return "DSConnection [No connection]";
        } else {
            return "DSConnection [" + m_Socket.getLocalSocketAddress().toString() + " -> " + m_Socket.getInetAddress().toString() + ":" + m_Socket.getPort() + "], (bytes: " + this.m_bytes_sent + " sent| " + this.m_bytes_received + " received)";
        }
    }
}
