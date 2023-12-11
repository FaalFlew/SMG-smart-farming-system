package no.ntnu.network.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import no.ntnu.network.client.clientinfo.BaseClientInfo;
import no.ntnu.network.client.clientinfo.ControlPanelClientInfo;
import no.ntnu.network.client.clientinfo.SensorActuatorClientInfo;
import no.ntnu.network.message.MessageHandler;
import no.ntnu.greenhouse.SensorReading;
import no.ntnu.tools.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;


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

            if("CONTROL_PANEL".equalsIgnoreCase(clientType)) {
                MessageHandler.sendAvailableCommandsListControlPanel(writer);
            }
            if ("SENSOR_ACTUATOR".equalsIgnoreCase(clientType)) {
                MessageHandler.sendAvailableCommandsListSensorActuator(writer);
            }

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

    /**
     * Handles disconnection of a client by removing its information based on its type (Control Panel or Sensor Actuator).
     * For Control Panel clients, their information is removed from the server's controlPanelClients list.
     * For Sensor Actuator clients, their information is removed from the server's sensorActuatorClients list.
     */
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
                case "all_control_panels":
                    // Send information about all connected CONTROL_PANEL clients
                    sendConnectedControlPanelClients(writer);
                    break;
                case "all_sensors":
                    // Send information about all connected sensor/actuator clients
                    sendConnectedSensorActuatorClients(writer);
                    break;
                case "command_to_sensor_actuator":
                    // send a command to a control panel, example {"type":"command_to_sensor_actuator","nodeid":"3", "ison":true}
                    handleCommandToSensorActuator(clientMessage);
                    break;

                    //TODO: get all sensor data at once
                case "sensor_data":
                    handleSensorData(clientMessage);
                    break;
                    //TODO: get_sensorvalue should take in "nodeid" for the node to retrieve the sensor value from and a "timer" as to how often (in seconds) to retrieve the data.
                case "get_sensorvalue":
                    // send a command to a control panel, example {"type":"command_to_control_panel","nodeid":"4"}
                    handleCommandToSensorActuator(clientMessage);
                    break;
                case "all_control_commands":
                    // TODO: send all control commands available for contorlpanel, store this case switch in a nice class first.
                    handleSensorData(clientMessage);
                    break;

                default:
                    Logger.error("Unknown message type: " + messageType);
            }
        } catch (Exception e) {
            Logger.error("Error handling message type: " + e.getMessage());
        }
    }

    /**
     * Retrieves the Node ID of the connected client based on its type (Control Panel or Sensor Actuator).
     *
     * @return The Node ID of the client, or -1 if the client type is unknown or the information is not available.
     */
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

    /**
     * Retrieves the client information based on the client's port number.
     *
     * @param clientPort The port number of the client.
     * @return The client information (ControlPanelClientInfo or SensorActuatorClientInfo), or null if not found.
     */
    private BaseClientInfo getClientInfoByPortNumber(int clientPort) {
        return ("CONTROL_PANEL".equalsIgnoreCase(clientType)) ? controlPanelClientInfoMap.get(clientPort) : sensorActuatorClientInfoMap.get(clientPort);

    }

    /**
     * Handles a command sent to a control panel client. Parses the command message to extract the nodeId and isOn values,
     * then forwards the command to the specified client.
     *
     * @param clientMessage The command message received from the control panel client.
     */
    private void handleCommandToSensorActuator(String clientMessage) {
        // Parse the command message and extract the nodeId
        JsonObject commandObject = gson.fromJson(clientMessage, JsonObject.class);
        int nodeId = commandObject.getAsJsonPrimitive("nodeid").getAsInt();
        boolean isOn = commandObject.getAsJsonPrimitive("ison").getAsBoolean();
        //notify the client that server retrieved the message.
        String response = MessageHandler.createSuccessResponse("SENSOR_DATA");
        writer.println(response);
        // Forward the command to the specified client
        SmartFarmingServer.forwardCommandToClient(nodeId,isOn);
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
     * Handles a command related to sensor values. Parses the command message to extract the nodeId and timer values,
     * then forwards the command to the specified client.
     *
     * @param clientMessage The command message received, containing information about the nodeId and timer.
     */
    private void handleCommandToSensorValue(String clientMessage) {
        // Parse the command message and extract the nodeId
        JsonObject commandObject = gson.fromJson(clientMessage, JsonObject.class);
        int nodeId = commandObject.getAsJsonPrimitive("nodeid").getAsInt();
        boolean isOn = commandObject.getAsJsonPrimitive("timer").getAsBoolean();

        // Forward the command to the specified client
        SmartFarmingServer.forwardCommandToClient(nodeId,isOn);
    }






}