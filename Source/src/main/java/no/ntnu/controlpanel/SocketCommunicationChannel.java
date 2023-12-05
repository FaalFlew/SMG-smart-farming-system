package no.ntnu.controlpanel;

import com.google.gson.JsonObject;
import no.ntnu.tools.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketCommunicationChannel implements CommunicationChannel {
    private final String serverAddress;
    private final int serverPort;
    private PrintWriter writer;
    private String clientType;

    public SocketCommunicationChannel(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    /**
     * Sends a control command to change the state of an actuator.
     *
     * @param nodeId     The unique identifier of the node associated with the actuator
     * @param actuatorId The identifier of the actuator to be controlled.
     * @param isOn       A boolean whether to turn the actuator on or off.
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
     * Open the communication channel with the client type.
     *
     * @param clientType The type of the client (e.g., "CONTROL_PANEL" or "SENSOR_ACTUATOR").
     * @return true if the communication channel is successfully opened, false otherwise.
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

    private void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    private Socket createSocket() {
        try {
            return new Socket(serverAddress, serverPort);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

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