package no.ntnu.network.server;

import com.google.gson.JsonObject;
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

/**
 * The ClientHandler class handles communication with a client connected to the server
 * It implements the Runnable interface to be used in a separate thread for concurrent handling of multiple clients.
 */
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private final String clientType;

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
    private void handleClient()  throws IOException {
        String clientMessage;
        try {
            while ((clientMessage = reader.readLine()) != null) {
                Logger.info("Received message from client " + clientType +": "+ clientMessage);

                handleMessage(clientMessage);

            }
        } catch (SocketException e) {
            Logger.info("Client disconnected: " + clientType);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles a received message from the client, determining its type and invoking the corresponding handler
     *
     * @param clientMessage The message received from the client
     */
    private void handleMessage(String clientMessage) {
        try {
            String messageType = MessageHandler.getMessageType(clientMessage);

            switch (messageType) {
                case "sensor_data":
                    // these "tdoo" methods may be done in the client class and the feedback or result of the call should be returned to server to furthar forward to contorl panel.
                    // TODO: method to check if node ID of the controlpanel we are sending data to exists.

                    handleSensorData(clientMessage);
                    break;
                case "actuator_control":
                    // these "tdoo" methods may be done in the client class and the feedback or result of the call should be
                    // returned to server to furthar forward to contorl panel.
                    // No need for too much network traffic, one call from control panel then a response back to the control panel.
                    // TODO: method to check if node ID and ID of the actutator we are sending a command to exists.
                    // TODO: Send the command to the actuator client then recieve response from client, forward the response to control panel.
                    handleActuatorControl(clientMessage);
                    break;

                case "all_control_commands":
                    // these "tdoo" methods may be done in the client class and the feedback or result of the call should be returned to server to furthar forward to contorl panel.
                    // TODO: method to check if node ID of the controlpanel we are sending data to exists.

                    handleSensorData(clientMessage);
                    break;
                case "shut_down":
                    // these "tdoo" methods may be done in the client class and the feedback or result of the call should be returned to server to furthar forward to contorl panel.
                    // TODO: method to check if node ID of the controlpanel we are sending data to exists.

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

        // Respond to the client if needed
        String response = MessageHandler.createSuccessResponse("ACTUATOR_CONTROL");
        writer.println(response);
    }

    //to be done Add more methods for handling different message types

    // Example method for simulating periodic sensor data broadcast
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
                sensorData.addProperty("type", "SENSOR_DATA");
                sensorData.addProperty("nodeId", nodeId);
                sensorData.addProperty("temperature", Math.random() * 30);
                sensorData.addProperty("humidity", Math.random() * 100);

                writer.println(sensorData.toString());
            }
        }, 0, 5000);
    }
}