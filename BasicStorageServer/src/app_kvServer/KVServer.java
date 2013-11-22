package app_kvServer;

import client.KVStore;
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
        LogSetup logSetup = new LogSetup("logs/server.log", Level.INFO);

        //Set up server
        final KVServer sv = new KVServer(55555);
        KVServer.logger.log(Level.INFO, "Server initialized.");
        //Start server in its own thread.
        (new Thread() {
            @Override
            public void run() {
                KVServer.logger.log(Level.INFO, "Server starting in its thread...");
                sv.runServer();
            }
        }).start();

        //Set up a store, connect and put/get a bit, disconnect, repeat a couple of times.
        try {
            KVStore mystore = new KVStore("localhost", 55555);
            KVServer.logger.log(Level.INFO, "KVStore initialized.");
            for (int client = 0; client < 10; client++) {
                mystore.connect();
                KVServer.logger.log(Level.INFO, "KVStore connected.");
                for (int request = 0; request < 10; request++) {
                    mystore.put("req_" + request, "req_" + request + " client " + client);
                    mystore.put("req_" + request, "req_" + request + " client update " + client);
                    //maybe sleep, run multithreaded clients
                    String kv = mystore.get("req_" + request).getValue();
                    if (!kv.equals("req_" + request + " client update " + client)) { //should be
                        KVServer.logger.log(Level.ERROR, "[ERROR] KVStore missmatch: " + request + ": " + kv + "!=" + request + "-" + client);
                    }
                }
                mystore.disconnect();
                KVServer.logger.log(Level.INFO, "KVStore disconnected.");
            }
        } catch (Exception ex) {
            KVServer.logger.log(Level.ERROR, null, ex);
        }

        //Wait 10 seconds then shut the server down.
        try {
            Thread.sleep(10000);
            sv.RequestShutdown();
        } catch (InterruptedException ex) {
            KVServer.logger.log(Level.ERROR, null, ex);
        }

    }
}
