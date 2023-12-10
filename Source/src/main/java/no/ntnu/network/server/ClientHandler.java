package no.ntnu.network.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import no.ntnu.network.client.clientinfo.BaseClientInfo;
import no.ntnu.network.client.clientinfo.ControlPanelClientInfo;
import no.ntnu.network.client.clientinfo.SensorActuatorClientInfo;
import no.ntnu.network.message.MessageHandler;
import no.ntnu.greenhouse.Actuator;
import no.ntnu.greenhouse.SensorReading;
import no.ntnu.tools.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static no.ntnu.network.server.SmartFarmingServer.*;

/**
 * The ClientHandler class handles communication with a client connected to the server
 * It implements the Runnable interface to be used in a separate thread for concurrent handling of multiple clients.
 */
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private final String clientType;
    private static final Gson gson = new Gson();

    /**
     * Constructs a new ClientHandler instance
     *
     * @param clientSocket The socket associated with the client
     * @param writer       The PrintWriter used for sending messages to the client
     * @param clientType   The type of the client (e.g., SENSOR, ACTUATOR).
     * @throws IOException If an I/O error occurs while setting up the input and output streams
     */
    public ClientHandler(Socket clientSocket, PrintWriter writer, String clientType) throws IOException {
        this.clientSocket = clientSocket;
        this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.writer = writer;
        this.clientType = clientType;
    }

    /**
     * Runs the client handling logic in a separate thread
     * Handles the client-specific logic, including periodic sensor data broadcast.
     */
    @Override
    public void run() {
        try {
            // Handle the client-specific logic, including periodic sensor data broadcast
            handleClient();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handles the communication with the client, including message reception and processing
     *
     * @throws IOException If an I/O error occurs during communication with the client.
     */
    private void handleClient() throws IOException {
        String clientMessage;
        try {
            while ((clientMessage = reader.readLine()) != null) {
                Logger.info("Received message from client " + clientType + " (NodeID: " + getClientNodeID() + "): " + MessageHandler.validateMessageFormat(clientMessage));

                handleMessage(clientMessage);
            }
        } catch (SocketException e) {
            Logger.info("Client disconnected: "+ clientType +" with nodeId:"+ getClientNodeID());
            handleClientDisconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleClientDisconnect() {
        if ("CONTROL_PANEL".equalsIgnoreCase(clientType)) {
            // Remove the control panel client information
            SmartFarmingServer.removeControlPanelClient(getClientNodeID());
        }
        if ("SENSOR_ACTUATOR".equalsIgnoreCase(clientType)) {
            SmartFarmingServer.removeSensorActuatorClient(getClientNodeID());
        }
    }

    /**
     * Handles a received message from the client, determining its type and invoking the corresponding handler
     *
     * @param clientMessage The message received from the client
     */
    private void handleMessage(String clientMessage) {
        try {
            // validate message
            clientMessage = MessageHandler.validateMessageFormat(clientMessage);
            String messageType = MessageHandler.getMessageType(clientMessage);

            switch (messageType) {
                case "sensor_data":
                    handleSensorData(clientMessage);
                    break;
                case "actuator_control":
                    handleActuatorControl(clientMessage);
                    break;
                case "all":
                    // Send information about all connected CONTROL_PANEL clients
                    sendConnectedControlPanelClients(writer);
                    break;
                case "all_sensors":
                    // Send information about all connected CONTROL_PANEL clients
                    sendConnectedSensorActuatorClients(writer);
                    break;
                case "command_to_control_panel":
                    // send a command to a control panel, examble {"type":"command_to_control_panel","nodeid":"4"}
                    handleCommandToControlPanel(clientMessage);
                    break;
                case "all_control_commands":
                    // TODO: send all control commands available for contorlpanel, store this case switch in a nice class first.
                    handleSensorData(clientMessage);
                    break;
                case "shut_down":
                    handleSensorData(clientMessage);
                    break;

                // Add more cases for other message types

                default:
                    Logger.error("Unknown message type: " + messageType);
            }
        } catch (Exception e) {
            Logger.error("Error handling message type: " + e.getMessage());
        }
    }


    // Add a method to retrieve the node ID from the ClientInfo
    public int getClientNodeID() {
        BaseClientInfo clientInfo = getClientInfoByPortNumber(clientSocket.getPort());

        if ("CONTROL_PANEL".equalsIgnoreCase(clientType) && clientInfo instanceof ControlPanelClientInfo) {
            return clientInfo.getNodeId();
        }
        if ("SENSOR_ACTUATOR".equalsIgnoreCase(clientType) && clientInfo instanceof SensorActuatorClientInfo) {
            return clientInfo.getNodeId();
        }

        return -1;
    }

    // Add a method to retrieve the ClientInfo based on the client address
    private BaseClientInfo getClientInfoByPortNumber(int clientPort) {
        return ("CONTROL_PANEL".equalsIgnoreCase(clientType)) ? controlPanelClientInfoMap.get(clientPort) : sensorActuatorClientInfoMap.get(clientPort);

    }

    private void handleCommandToControlPanel(String clientMessage) {
        // Parse the command message and extract the nodeId
        JsonObject commandObject = gson.fromJson(clientMessage, JsonObject.class);
        int nodeId = commandObject.getAsJsonPrimitive("nodeid").getAsInt();

        // Forward the command to the specified client
        SmartFarmingServer.forwardCommandToClient(nodeId, clientMessage);
    }
    /**
     * Handles sensor data received from the client, parsing and processing the data
     *
     * @param clientMessage The sensor data message received from the client
     */
    private void handleSensorData(String clientMessage) {
        List<SensorReading> sensorDataList = MessageHandler.parseSensorDataMessage(clientMessage);

        // TODO: Implement sensor data handling logic

        // Respond to the client if needed
        String response = MessageHandler.createSuccessResponse("SENSOR_DATA");
        writer.println(response);
    }

    /**
     * Handles actuator control command received from the client, parsing and processing the command
     *
     * @param clientMessage The actuator control command message received from the client
     */
    private void handleActuatorControl(String clientMessage) {
        List<Actuator> actuatorStatusList = MessageHandler.parseActuatorStatusMessage(clientMessage);

        // TODO: Implement actuator control logic

        // Respond to the client
        String response = MessageHandler.createSuccessResponse("ACTUATOR_CONTROL");
        writer.println(response);
    }

    //to be done Add more methods for handling different message types

    //  method for simulating periodic sensor data broadcast should be used along the starting value of sensornode
    /**
     * Simulates periodic sensor data broadcast to the client
     *
     * @param nodeId The ID of the node for which to simulate sensor data broadcast
     */
    private void simulatePeriodicSensorDataBroadcast(int nodeId) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Simulate sensor data
                JsonObject sensorData = new JsonObject();
                sensorData.addProperty("type", "sensor_data");
                sensorData.addProperty("nodeid", nodeId);
                sensorData.addProperty("temperature", Math.random() * 30);
                sensorData.addProperty("humidity", Math.random() * 100);

                writer.println(sensorData.toString());
            }
        }, 0, 5000);
    }
}