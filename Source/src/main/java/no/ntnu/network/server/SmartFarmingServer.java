package no.ntnu.network.server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import no.ntnu.network.client.clientinfo.ControlPanelClientInfo;
import no.ntnu.network.client.clientinfo.SensorActuatorClientInfo;
import no.ntnu.network.message.MessageHandler;
import no.ntnu.tools.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The SmartFarmingServer class represents the main server for Smart Farming communication
 * It accepts incoming client connections, identifies their type, and delegates handling to separate threads using a thread pool
 */
public class SmartFarmingServer {

    public static final int PORT = 6019;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private static final List<PrintWriter> connectedClients = new ArrayList<>();
    public static final List<ControlPanelClientInfo> controlPanelClients = new CopyOnWriteArrayList<>();
    public static final Map<Integer, ControlPanelClientInfo> controlPanelClientInfoMap = new HashMap<>();
    public static final List<SensorActuatorClientInfo> sensorActuatorClients = new CopyOnWriteArrayList<>();
    public static final Map<Integer, SensorActuatorClientInfo> sensorActuatorClientInfoMap = new HashMap<>();

    // TODO: implement heartbeat functionality to disconnect and remove clients who are inactive.
    private static final Map<Integer, Long> lastHeartbeatMap = new HashMap<>();

    private static final Gson gson = new Gson();


    /**
     * The main entry point for starting the Smart Farming Server
     * It initializes the server socket, listens for incoming connections, and delegates handling to ClientHandler threads
     *
     * @param args Command-line arguments (not used in this implementation)
     */
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            // Call the method to send a warning to all connected clients before shutting down
            sendShutdownToAllClients("Server is closing...");
            // Perform cleanup tasks
            executorService.shutdown();
        }));
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            Logger.info("Smart Farming Server started, waiting for client connections...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                PrintWriter clientWriter = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String clientType = getClientType(reader);
                Logger.info("Client connected: " + clientType +" "+ clientSocket.getInetAddress().getHostName() +
                        " [" + clientSocket.getPort() + "]");

                connectedClients.add(clientWriter);

                if ("CONTROL_PANEL".equalsIgnoreCase(clientType)) {
                    processControlPanelClient(clientSocket, clientWriter, reader);
                    // Send the connected control panel clients to the new control panel
                    sendConnectedControlPanelClients(clientWriter);
                }
                if ("SENSOR_ACTUATOR".equalsIgnoreCase(clientType)) {
                    processSensorActuatorClient(clientSocket, clientWriter, reader);
                    sendConnectedSensorActuatorClients(clientWriter);
                }

                executorService.execute(new ClientHandler(clientSocket, clientWriter, clientType));
            }
        } catch (IOException e) {
            Logger.error("Error starting the server: " + e.getMessage());
        } finally {
            // Call the method to send a warning to all connected clients before shutting down
            sendShutdownToAllClients("Server is closing...");

            // Perform any other cleanup or shutdown tasks here
            executorService.shutdown();
        }
    }

    /**
     * Forwards a control command to the Sensor Actuator client with the specified nodeId.
     * Updates the client's isOn status, creates a structured command message, and sends it to the client.
     *
     * @param nodeId The identifier of the target Sensor Actuator client.
     * @param isOn   The new status to set for the client.
     */
    public static void forwardCommandToClient(int nodeId, boolean isOn) {
        for (SensorActuatorClientInfo sensorActuatorClientInfo : sensorActuatorClients) {
            if (sensorActuatorClientInfo.getNodeId() == nodeId) {
                sensorActuatorClientInfo.setOn(isOn);
                PrintWriter clientWriter = sensorActuatorClientInfo.getClientWriter();
                if (clientWriter != null) {
                    // Create a structured command message
                    JsonObject commandObject = new JsonObject();
                    commandObject.addProperty("type", "control_command");
                    commandObject.addProperty("ison", isOn);

                    // Send the command message to the client
                    clientWriter.println(commandObject.toString());
                    return; // Command forwarded successfully
                }
            }
        }
        // Handle the case when the client with the specified nodeId is not found
        Logger.error("Client with nodeId " + nodeId + " not found");
    }

    /**
     * Removes a Control Panel client from the server's records based on the specified nodeId.
     * Removes the client information from the list and the map.
     *
     * @param nodeId The identifier of the Control Panel client to be removed.
     */
    public static void removeControlPanelClient(int nodeId) {
        controlPanelClients.removeIf(controlPanelClientInfo -> controlPanelClientInfo.getNodeId() == nodeId);

        // Remove from the map as well
        controlPanelClientInfoMap.values().removeIf(controlPanelClientInfo -> controlPanelClientInfo.getNodeId() == nodeId);

        Logger.info("Control Panel client removed: nodeId=" + nodeId);
    }

    /**
     * Removes a Sensor Actuator client from the server's records based on the specified nodeId.
     * Removes the client information from the list and the map.
     *
     * @param nodeId The identifier of the Sensor Actuator client to be removed.
     */
    public static void removeSensorActuatorClient(int nodeId) {
        sensorActuatorClients.removeIf(sensorActuatorClientInfo -> sensorActuatorClientInfo.getNodeId() == nodeId);

        // Remove from the map as well
        sensorActuatorClientInfoMap.values().removeIf(sensorActuatorClientInfo -> sensorActuatorClientInfo.getNodeId() == nodeId);

        Logger.info("Sensor Actuator client removed: nodeId=" + nodeId);
    }

    /**
     * Processes a new Control Panel client by reading information from the provided BufferedReader,
     * parsing the JSON string, and storing the client information in the server's records.
     *
     * @param clientSocket The socket associated with the Control Panel client.
     * @param writer The PrintWriter for the Control Panel client.
     * @param reader The BufferedReader connected to the client's input stream.
     * @return The PrintWriter for the Control Panel client.
     * @throws IOException If an I/O error occurs while processing the client.
     */
    private static PrintWriter processControlPanelClient(Socket clientSocket, PrintWriter writer, BufferedReader reader) throws IOException {

        String jsonInfo = reader.readLine();

        // Parse the JSON string to extract the required information
        JsonObject infoObject = gson.fromJson(jsonInfo, JsonObject.class);

        int nodeId = infoObject.getAsJsonPrimitive("nodeId").getAsInt();

        // Store the client information in the list
        ControlPanelClientInfo controlPanelClientInfo = new ControlPanelClientInfo(nodeId, clientSocket.getInetAddress().getHostName(),clientSocket.getPort(), writer);
        controlPanelClients.add(controlPanelClientInfo);

        // Store ClientInfo in the map
        controlPanelClientInfoMap.put(clientSocket.getPort(), controlPanelClientInfo);

        return writer;
    }

    /**
     * Processes a new Sensor Actuator client by reading additional information from the provided BufferedReader,
     * parsing the JSON string, and storing the client information in the server's records.
     *
     * @param clientSocket The socket associated with the Sensor Actuator client.
     * @param writer The PrintWriter for the Sensor Actuator client.
     * @param reader The BufferedReader connected to the client's input stream.
     * @return The PrintWriter for the Sensor Actuator client.
     * @throws IOException If an I/O error occurs while processing the client.
     */
    private static PrintWriter processSensorActuatorClient(Socket clientSocket, PrintWriter writer, BufferedReader reader) throws IOException {
        // Read additional information from SENSOR_ACTUATOR client
        String jsonInfo = reader.readLine();

        // Parse the JSON string to extract the required information
        JsonObject infoObject = gson.fromJson(jsonInfo, JsonObject.class);

        int nodeId = infoObject.getAsJsonPrimitive("nodeId").getAsInt();
        int actuatorId = infoObject.getAsJsonPrimitive("actuatorId").getAsInt();
        String actuatorType = infoObject.getAsJsonPrimitive("actuatorType").getAsString();
        boolean isOn = infoObject.getAsJsonPrimitive("isOn").getAsBoolean();
        String sensorType = infoObject.getAsJsonPrimitive("sensorType").getAsString();
        double sensorValue = infoObject.getAsJsonPrimitive("sensorValue").getAsDouble();

        // Store the client information in the list (**!!important order of params!!**)
        SensorActuatorClientInfo sensorActuatorClientInfo = new SensorActuatorClientInfo(
                nodeId,
                actuatorId,
                actuatorType,
                isOn,
                sensorType,
                sensorValue,
                clientSocket.getInetAddress().getHostName(),
                clientSocket.getPort(),
                writer
        );
        sensorActuatorClients.add(sensorActuatorClientInfo);

        // Store ClientInfo in the map for future reference
        sensorActuatorClientInfoMap.put(clientSocket.getPort(), sensorActuatorClientInfo);

        return writer;
    }


    /**
     * Sends a message to the specified Control Panel client containing information about all connected Control Panel clients.
     *
     * @param clientWriter The PrintWriter associated with the Control Panel client.
     */
    public static void sendConnectedControlPanelClients(PrintWriter clientWriter) {
        JsonObject response = new JsonObject();
        response.addProperty("type", "all");
        JsonArray clientsArray = new JsonArray();

        for (ControlPanelClientInfo controlPanelClientInfo : controlPanelClients) {
            JsonObject clientObject = new JsonObject();
            clientObject.addProperty("nodeid", controlPanelClientInfo.getNodeId());
            clientObject.addProperty("clientPort", controlPanelClientInfo.getClientPort());
            clientObject.addProperty("clientAddress", controlPanelClientInfo.getClientAddress());


            clientsArray.add(clientObject);
        }

        response.add("connectedControlPanelClients", clientsArray);
        clientWriter.println(response.toString());
    }

    /**
     * Sends a message to the specified Sensor Actuator client containing information about all connected Sensor Actuator clients.
     *
     * @param clientWriter The PrintWriter associated with the Sensor Actuator client.
     */

    public static void sendConnectedSensorActuatorClients(PrintWriter clientWriter) {
        JsonObject response = new JsonObject();
        response.addProperty("type", "all_sensors");
        JsonArray clientsArray = new JsonArray();

        for (SensorActuatorClientInfo sensorActuatorClientInfo : sensorActuatorClients) {
            JsonObject clientObject = new JsonObject();
            clientObject.addProperty("nodeid", sensorActuatorClientInfo.getNodeId());
            clientObject.addProperty("actuatorid", sensorActuatorClientInfo.getActuatorId());
            clientObject.addProperty("actuatortype", sensorActuatorClientInfo.getActuatorType());
            clientObject.addProperty("ison", sensorActuatorClientInfo.getIsOn());
            clientObject.addProperty("sensortype", sensorActuatorClientInfo.getSensorType());
            clientObject.addProperty("sensorvalue", sensorActuatorClientInfo.getSensorValue());
            clientObject.addProperty("clientPort", sensorActuatorClientInfo.getClientPort());
            clientObject.addProperty("clientAddress", sensorActuatorClientInfo.getClientAddress());

            clientsArray.add(clientObject);
        }

        response.add("connectedSensorActuatorClients", clientsArray);
        clientWriter.println(response.toString());
    }


    /**
     * Reads and returns the client type from the provided BufferedReader
     *
     * @param reader The BufferedReader connected to the client's input stream
     * @return The client type as a String
     * @throws IOException If an I/O error occurs while reading the client type
     */
    public static String getClientType(BufferedReader reader) throws IOException {
        return reader.readLine();
    }

    /**
     * Sends a shutdown message to all connected clients with the specified message.
     *
     * @param message The message to be included in the shutdown message.
     */
    private static void sendShutdownToAllClients(String message) {
        for (PrintWriter clientWriter : connectedClients) {
            try {
                // Create a shutdown message
                String shutdownMessage = MessageHandler.createShutdownMessage(message);

                // Send the shutdown message to the client
                clientWriter.println(shutdownMessage);
            } catch (Exception e) {
                Logger.error("Error sending shutdown message to client: " + e.getMessage());
            }
        }
    }

    /**
     * Sends a warning message to all connected clients before shutting down the server
     * @param message The message to be sent to all connected clients.

     */
    private static void sendWarningToAllClients(String message) {
        for (PrintWriter clientWriter : connectedClients) {
            try {
                // Create a warning message
                String warningMessage = MessageHandler.createWarningMessage(message);

                // Send the warning message to the client
                clientWriter.println(warningMessage);
            } catch (Exception e) {
                Logger.error("Error sending warning to client: " + e.getMessage());
            }
        }
    }

}