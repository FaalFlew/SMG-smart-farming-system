package no.ntnu.network.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import no.ntnu.controlpanel.CommunicationChannel;
import no.ntnu.controlpanel.ExtendedCommunicationChannel;
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
public class SocketCommunicationChannel implements ExtendedCommunicationChannel {

    private final String serverAddress;
    private final int serverPort;
    private Socket clientSocket;
    private PrintWriter writer;
    private static final Gson gson = new Gson();

    private volatile boolean serverShutdownReceived = false;

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


    @Override
    public void sendGetSensorData(int nodeId, long timer) {
        JsonObject jsonMessage = new JsonObject();
        jsonMessage.addProperty("type", "GET_SENSOR_DATA");
        jsonMessage.addProperty("nodeId", nodeId);
        jsonMessage.addProperty("timer", timer);

        String message = jsonMessage.toString();
        if (writer != null) {
            writer.println(message);
        }
    }
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

    @Override
    public void sendSensorData(int nodeId, int actuatorId, String actuatorType, boolean isOn, String sensorType, double sensorValue) {
        JsonObject jsonMessage = new JsonObject();
        jsonMessage.addProperty("type", "SENSOR_DATA");
        jsonMessage.addProperty("nodeId", nodeId);
        jsonMessage.addProperty("actuatorId", actuatorId);
        jsonMessage.addProperty("actuatorType", actuatorType);
        jsonMessage.addProperty("isOn", isOn);
        jsonMessage.addProperty("sensorType", sensorType);
        jsonMessage.addProperty("sensorValue", sensorValue);

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
        clientSocket = createSocket();

        if (clientSocket != null) {
            boolean initialized = initializeWriter(clientSocket, clientType);
            if (initialized) {
                new Thread(this::readAndSendMessages).start();
                new Thread(new ClientListener(this)).start();
                Logger.info("Communication channel opened successfully");
                return true;
            } else {
                closeSocket(clientSocket);
                Logger.warning("Failed to initialize writer. Communication channel not opened.");
            }
        } else {
            Logger.warning("Failed to create socket. Communication channel not opened.");
        }
        return false;
    }

    /**
     * Initializes the PrintWriter for sending messages and performs a handshake with the server by sending the client type
     *
     * @param socket The socket for communication with the server
     * @param clientType The clientType for communication with the server
     * @return true if the writer is successfully initialized, false otherwise
     */
    private boolean initializeWriter(Socket socket, String clientType) {
        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(clientType);
            Logger.info("Writer initialized with client type: " + clientType);
            return true;
        } catch (IOException e) {
            Logger.error("Error initializing writer: " + e.getMessage());
            return false;
        }
    }


    /**
     * Reads and sends user-entered messages to the server in a separate thread.
     */
    private void readAndSendMessages() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try {
            while (!serverShutdownReceived) {
                Logger.info("Enter a message to send to the server (or 'exit' to quit): ");
                String userInput = reader.readLine();

                if ("exit".equalsIgnoreCase(userInput)) {
                    closeSocketAndWriter();
                    break;
                }

                if (isMessageFormatValidJSON(userInput)) {
                    sendMessage(userInput);
                } else {
                    Logger.error("Invalid JSON format. Please enter a valid JSON message.");
                }
            }
        } catch (IOException e) {
            Logger.error("Error reading and sending messages: " + e.getMessage());
        }
    }

    /**
     * sets the shutdown value
     * @param ServerShutdownReceived shutdown value
     */
    public void setShutdownReceived(boolean ServerShutdownReceived) {
        this.serverShutdownReceived = ServerShutdownReceived;
    }

    /**
     * validates client input at client level
     * @param userInput the client input
     * @return boolean is valid or invalid
     */
    private boolean isMessageFormatValidJSON(String userInput) {
        try {
            gson.fromJson(userInput, JsonObject.class);
            return true;
        } catch (JsonSyntaxException e) {
            Logger.error("Error parsing JSON: " + e.getMessage());
            return false;
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
            Logger.error("Failed to create a socket: " + e.getMessage());
            return null;
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
                closeWriter();
                socket.close();
                Logger.info("Socket closed successfully");
            } catch (IOException e) {
                Logger.error("Failed to close socket: " + e.getMessage());
            }
        } else {
            Logger.warning("Socket is already closed or null. No action taken.");
        }
    }
    /**
    * Closes the socket and associated resources
    */
    private void closeWriter() {
        if (writer != null) {
            writer.close();
            Logger.info("Writer closed successfully");
        } else {
            Logger.warning("Writer is already closed or null. No action taken.");
        }
    }

    /**
     * close both the socket and writer
     */
    public void closeSocketAndWriter() {
        closeWriter();
        closeSocket(clientSocket);
    }

    /**
     * gets the client socket
     * @return the client socket
     */
    public Socket getClientSocket() {
        return clientSocket;
    }
}