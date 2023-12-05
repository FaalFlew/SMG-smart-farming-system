package no.ntnu.controlpanel;

import com.google.gson.JsonObject;
import no.ntnu.tools.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * The SocketCommunicationChannel class implements the CommunicationChannel interface for communication
 * with the server using a socket connection
 */
public class SocketCommunicationChannel implements CommunicationChannel {
    private final String serverAddress;
    private final int serverPort;
    private PrintWriter writer;
    private String clientType;

    /**
     * Constructs a new SocketCommunicationChannel instance with the specified server address and port
     *
     * @param serverAddress The server address for establishing a socket connection
     * @param serverPort    The server port for establishing a socket connection
     */
    public SocketCommunicationChannel(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    /**
     * Sends a control command to change the state of an actuator.
     *
     * @param nodeId     The unique identifier of the node associated with the actuator
     * @param actuatorId The identifier of the actuator to be controlled
     * @param isOn       A boolean whether to turn the actuator on or off
     */
    @Override
    public void sendActuatorChange(int nodeId, int actuatorId, boolean isOn) {
        JsonObject jsonMessage = new JsonObject();
        jsonMessage.addProperty("type", "ACTUATOR_CONTROL");
        jsonMessage.addProperty("nodeId", nodeId);
        jsonMessage.addProperty("actuatorId", actuatorId);
        jsonMessage.addProperty("isOn", isOn);

        String message = jsonMessage.toString();
        if (writer != null) {
            writer.println(message);
        }
    }

    /**
     * Open the communication channel with the client type
     *
     * @param clientType The type of the client (e.g., "CONTROL_PANEL" or "SENSOR_ACTUATOR")
     * @return true if the communication channel is successfully opened, false otherwise
     */
    @Override
    public boolean open(String clientType) {
        this.clientType = clientType;
        Socket socket = createSocket();

        if (socket != null) {
            boolean initialized = initializeWriter(socket);
            if (initialized) {
                // Start a separate thread for continuous message sending
                new Thread(this::readAndSendMessages).start();
                return true;
            } else {
                closeSocket(socket);
            }
        }
        return false;
    }

    /**
     * Reads and sends user-entered messages to the server in a separate thread.
     */
    private void readAndSendMessages() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try {
            while (true) {
                Logger.info("Enter a message to send to the server (or 'exit' to quit): ");
                String userInput = reader.readLine();

                if ("exit".equalsIgnoreCase(userInput)) {
                    break;
                }

                // Send the user-entered message to the server
                sendMessage(userInput);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a message to the server
     *
     * @param message The message to be sent to the server
     */
    private void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    /**
     * Creates a socket connection to the server.
     *
     * @return The created Socket object for communication.
     */
    private Socket createSocket() {
        try {
            return new Socket(serverAddress, serverPort);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Initializes the PrintWriter for sending messages and performs a handshake with the server by sending the client type
     *
     * @param socket The socket for communication with the server
     * @return true if the writer is successfully initialized, false otherwise
     */
    private boolean initializeWriter(Socket socket) {
        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
            // Send the client type during the handshake
            writer.println(clientType);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Closes the socket and associated resources
     *
     * @param socket The socket to be closed
     */
    private void closeSocket(Socket socket) {
        if (socket != null && !socket.isClosed()) {
            try {
                if (writer != null) {
                    writer.close();
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}