import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private final Gson gson;

    public ClientHandler(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.writer = new PrintWriter(clientSocket.getOutputStream(), true);
        this.gson = new Gson();
    }

    @Override
    public void run() {
        try {
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

    private void handleSensorData(JsonObject jsonMessage) {
        // Implement sensor data handling logic
        // Extract necessary information from the JSON message
        int nodeId = jsonMessage.get("nodeId").getAsInt();

        // Respond to the client if needed
        JsonObject response = new JsonObject();
        response.addProperty("status", "success");
        writer.println(response.toString());
    }

    private void handleActuatorControl(JsonObject jsonMessage) {
        // Implement actuator control logic
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
}