package app_kvServer;

import common.messages.KVMessage;
import common.messages.KVBasicMessage;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import logger.LogSetup;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class KVServer {

    public static final Logger logger = Logger.getRootLogger();
    private final ConcurrentHashMap<String, String> local_hashmap = new ConcurrentHashMap<String, String>();

    private boolean shutdown_requested = false;

    public boolean isShutdownRequested() {
        return this.shutdown_requested;
    }

    public void RequestShutdown() {
        this.shutdown_requested = true;
        try {
            serverSocket.close();
        } catch (IOException e) {
            logger.error("Error! Unable to close socket on port: " + port, e);
        }
    }
    private final int port;

    public int getPort() {
        return this.port;
    }
    private final ServerSocket serverSocket;

    public ServerSocket getServerSocket() {
        return this.serverSocket;
    }

    /**
     * Start KV Server at given port
     *
     * @param port given port for storage server to operate
     * @throws java.io.IOException
     */
    public KVServer(int port) throws IOException {
        this.port = port;
        this.serverSocket = new ServerSocket(this.port);
    }

    public void runServer() {
        while (!isShutdownRequested()) {
            try {
                Socket client = serverSocket.accept();
                new Thread(new ServerThread(this, client)).start();

                logger.info("Connected to " + client.getInetAddress().getHostName() + " on port " + client.getPort());
            } catch (IOException e) {
                logger.error("Error! Unable to establish connection. \n", e);
            }
        }
    }

    public synchronized KVBasicMessage put(String key, String value) {
        try {
            if (this.local_hashmap.containsKey(key)) {
                this.local_hashmap.put(key, value);
                return new KVBasicMessage(key, value, KVMessage.StatusType.PUT_UPDATE);
            } else {
                this.local_hashmap.put(key, value);
                return new KVBasicMessage(key, value, KVMessage.StatusType.PUT_SUCCESS);
            }
        } catch (Exception e) {
            return new KVBasicMessage(key, value, KVMessage.StatusType.PUT_ERROR);
        }

    }

    public synchronized KVBasicMessage get(String key) {
        if (this.local_hashmap.containsKey(key)) {
            String value = this.local_hashmap.get(key);
            return new KVBasicMessage(key, value, KVMessage.StatusType.GET_SUCCESS);
        }
        return new KVBasicMessage(key, null, KVMessage.StatusType.GET_ERROR);
    }

    public static void main(String[] args) throws IOException {
        //args=new String[]{"50000"};
        try {
            LogSetup logSetup = new LogSetup("logs/server.log", Level.ALL);
            if (args.length != 1) {
                System.out.println("Error! Invalid number of arguments!");
                System.out.println("Usage: Server <port>!");
            } else {
                int port = Integer.parseInt(args[0]);
                System.out.println("Starting Server thread using port !"+port);
                final KVServer sv = new KVServer(port);
                (new Thread() {
                    @Override
                    public void run() {
                        System.out.println("Running server...");
                        sv.runServer();
                    }
                }).start();
            }
        } catch (IOException e) {
            System.out.println("Error! Unable to initialize logger!");
            e.printStackTrace();
            System.exit(1);
        } catch (NumberFormatException nfe) {
            System.out.println("Error! Invalid argument <port>! Not a number!");
            System.out.println("Usage: Server <port>!");
            System.exit(1);
        }
    }
}
