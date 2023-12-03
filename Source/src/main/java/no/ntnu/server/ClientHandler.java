// ClientHandler.java
package no.ntnu.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private final Gson gson;

    public ClientHandler(Socket clientSocket, PrintWriter writer) throws IOException {
        this.clientSocket = clientSocket;
        this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.writer = writer;
        this.gson = new Gson();
    }

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

    private void handleClient() throws IOException {
        String clientMessage;
        while ((clientMessage = reader.readLine()) != null) {
            System.out.println("Received message from client: " + clientMessage);

            // Parse JSON message
            JsonObject jsonMessage = gson.fromJson(clientMessage, JsonObject.class);

            // Handle the message based on its type
            String messageType = jsonMessage.get("type").getAsString();
            switch (messageType) {
                case "SENSOR_DATA":
                    handleSensorData(jsonMessage);
                    break;
                case "ACTUATOR_CONTROL":
                    handleActuatorControl(jsonMessage);
                    break;
                // Add more cases for other message types

                default:
                    System.out.println("Unknown message type: " + messageType);
            }
        }
    }

    private void handleSensorData(JsonObject jsonMessage) {
        //TODO: Implement sensor data handling logic

        // Extract necessary information from the JSON message
        int nodeId = jsonMessage.get("nodeId").getAsInt();

        // Respond to the client if needed
        JsonObject response = new JsonObject();
        response.addProperty("status", "success");
        writer.println(response.toString());
    }

    private void handleActuatorControl(JsonObject jsonMessage) {
        // TODO: Implement actuator control logic

        //Extract necessary information from the JSON message
        int nodeId = jsonMessage.get("nodeId").getAsInt();
        int actuatorId = jsonMessage.get("actuatorId").getAsInt();
        boolean isOn = jsonMessage.get("isOn").getAsBoolean();

        // Respond to the client if needed
        JsonObject response = new JsonObject();
        response.addProperty("status", "success");
        writer.println(response.toString());
    }

    // Add more methods for handling different message types

    // Example method for simulating periodic sensor data broadcast
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
