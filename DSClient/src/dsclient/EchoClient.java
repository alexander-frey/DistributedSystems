/*
 */
package dsclient;

import java.util.Scanner;
import DistributedSystems.DSConnection;
import java.io.IOException;
import java.net.UnknownHostException;
import org.apache.log4j.*;

/**
 * CLI client for Distributed Systems echo assignment.
 *
 * @author Maximilian This Class implements a CLI client to use as the frontend for (right now) establishing connections to the EchoServer
 */
public class EchoClient {

    private static DSConnection connection;
    private static Logger l4jlogger;

    /**
     * Main program, executes the connection setup and command input loop.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        connection = new DSConnection();
        init_logger();
        //default test server: 131.159.52.1:50000
        //Main command loop here
        String cmd;
        while (!"quit".equals(cmd = console_readline(""))) {
            //CMD: connect
            if (cmd.startsWith("connect ")) {
                //Parse input:
                String[] cmd_args = cmd.substring(8).split(" |:");
                if (cmd_args.length < 2) {
                    l4jlogger.warn("Invalid connect syntax. Use 'connect <ip> <port>'.");
                } else {
                    String host = cmd_args[0];
                    try {
                        int port;
                        if (host.equals("default")) { //shortcut for testing: "connect default x"
                            host = "131.159.52.1";
                            port = 50000;
                        } else {
                            port = Integer.parseInt(cmd_args[1], 10);
                        }
                        l4jlogger.info("Connecting to host:" + host + " via port: " + port + " (5 second timeout) ...");
                        connection.connect(host, port, 5000);
                        l4jlogger.info("... successfully connected.");
                    } catch (UnknownHostException uhex) {
                        l4jlogger.error("Invalid connect syntax: '" + host + "' was not a valid remote address."); //, uhex);
                    } catch (NumberFormatException nfex) {
                        l4jlogger.error("Invalid connect syntax: '" + cmd_args[1] + "' was not a valid port."); //, nfex);
                    } catch (IOException ioex) {
                        l4jlogger.error("IO Error: " + ioex.getMessage()); //, ioex);
                    } finally {

                    }
                }
            } //CMD: disconnect
            else if (cmd.equals("disconnect")) {
                l4jlogger.info("Attempting to disconnect...");
                try {
                    connection.disconnect();
                    l4jlogger.info("... successfully disconnected.");
                } catch (IOException ioex) {
                    l4jlogger.error("IO Error: " + ioex.getMessage(), ioex);
                } finally {

                }
            } //CMD: coninfo
            else if (cmd.equals("coninfo")) {
                l4jlogger.info("Current connection: " + connection.ToString());
            } //CMD: send
            else if (cmd.startsWith("send ")) {
                String message = cmd.substring(5);
                //Sanity checks for message length...
                byte[] payload = message.getBytes();
                try {
                    l4jlogger.info("Attempting to send '" + message + "', length:" + message.length() + " ...");
                    connection.send(payload);
                    l4jlogger.info("... sending complete.");
                    l4jlogger.info("Waiting for echo response from server... ");
                    byte[] response = connection.receive();
                    String resmessage = new String(response);

                    l4jlogger.info("... received response of length: " + response.length + " '" + resmessage + "'");
                } catch (IOException ioex) {
                    l4jlogger.error("IO Error: " + ioex.getMessage()); //, ioex);
                    //connection.disconnect();
                } finally {

                }
            } //CMD: logLevel
            else if (cmd.startsWith("logLevel ")) {
                String req_level = cmd.substring(9);
                if (req_level.equals("ALL")) {
                    l4jlogger.setLevel(Level.ALL);
                } else if (req_level.equals("TRACE")) {
                    l4jlogger.setLevel(Level.TRACE);
                } else if (req_level.equals("DEBUG")) {
                    l4jlogger.setLevel(Level.DEBUG);
                } else if (req_level.equals("INFO")) {
                    l4jlogger.setLevel(Level.INFO);
                } else if (req_level.equals("WARN")) {
                    l4jlogger.setLevel(Level.WARN);
                } else if (req_level.equals("ERROR")) {
                    l4jlogger.setLevel(Level.ERROR);
                } else if (req_level.equals("FATAL")) {
                    l4jlogger.setLevel(Level.FATAL);
                } else if (req_level.equals("OFF")) {
                    l4jlogger.setLevel(Level.OFF);
                } else {
                    l4jlogger.info("Logging level not recognized. Syntax: 'logLevel <ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF>'");
                }
                l4jlogger.info("Logging is now:" + l4jlogger.getLevel().toString()); //Possibly use console?
            } //CMD: help
            else if (cmd.equals("help")) {
                print_help();
            } //CMD: unrecoqnized.
            else {
                l4jlogger.warn("'" + cmd + "' was not recognized, displaying help...");
                print_help();
            }
        }
        try {
            connection.disconnect();
            l4jlogger.info("... successfully disconnected.");
        } catch (IOException ioex) {
            //l4jlogger.error("IO Error: " + ioex.getMessage(), ioex);
        } finally {

        }
        l4jlogger.info("Application quit");
    }

    private static void init_logger() {
        l4jlogger = Logger.getRootLogger();
        try {
            SimpleLayout loglayout = new SimpleLayout();
            l4jlogger.addAppender(new ConsoleAppender(loglayout));
            l4jlogger.addAppender(new FileAppender(loglayout, "echoclient.log", true));
            l4jlogger.setLevel(Level.ALL);
        } catch (Exception ex) {
            System.err.println("Unable to initialize logger: " + ex.getMessage());
        }
    }

    /**
     * Read line from console
     * <p>
     * Reads a line (until enter) from System.in using the Scanner utility class.
     *
     * @param prompt A message to display before waiting for input.
     * @return The entered line.
     */
    private static String console_readline(String prompt) {
        System.out.print("EchoClient> "+prompt);
        Scanner in = new Scanner(System.in);
        String s = in.nextLine();
        return s;
    }

    /**
     * Help display
     * <p>
     * Displays the available commands and their syntax in the console.
     */
    private static void print_help() {
        l4jlogger.info("Supported commands:\n"
                + "\t'help' : Displays this message.\n"
                + "\t'logLevel <ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF>' : Sets level of logging.\n"
                + "\t'quit' : Exits the program.\n"
                + "\t'connect <ip> <port>' : Attempts to establish a connection.\n"
                + "\t'disconnect' : Terminates an existing connection.\n"
                + "\t'send <message>' : Attempts to transmit string message over established connection.\n");
    }
}
