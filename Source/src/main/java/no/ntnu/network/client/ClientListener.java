package no.ntnu.network.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import no.ntnu.controlpanel.ExtendedCommunicationChannel;
import no.ntnu.tools.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientListener implements Runnable {

    private final ExtendedCommunicationChannel communicationChannel;
    private static final Gson gson = new Gson();

    public ClientListener(ExtendedCommunicationChannel communicationChannel) {
        this.communicationChannel = communicationChannel;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(communicationChannel.getClientSocket().getInputStream()))) {
            String serverMessage;
            while ((serverMessage = reader.readLine()) != null) {
                Logger.info("Received message from server: " + serverMessage);

                // Handle the server message as needed
                if (isShutdownNotification(serverMessage)) {
                    Logger.warning("Server shutdown notification received. Closing the client socket and writer.");
                    communicationChannel.setShutdownReceived(true);
                    communicationChannel.closeSocketAndWriter();
                    break;
                }

                // TODO: Continue handling other server messages
                // Example: messagehandler.handleServerMessage(serverMessage);
            }
        } catch (IOException e) {
            Logger.error("Error reading server message: " + e.getMessage());
        }
    }

    private void handleServerMessage(String serverMessage) {
        // Parse the server message to determine its type
        JsonObject jsonMessage = gson.fromJson(serverMessage, JsonObject.class);
        String messageType = jsonMessage.get("type").getAsString();

        // Handle different types of messages
        switch (messageType) {
            case "ACTUATOR_CONTROL":
                handleActuatorControlMessage(jsonMessage);
                break;
            default:
                Logger.warning("Unknown message type: " + messageType);
                break;
        }
    }

    private void handleActuatorControlMessage(JsonObject jsonMessage) {
        // Extract information from the message and take appropriate action
        int nodeId = jsonMessage.get("nodeId").getAsInt();
        int actuatorId = jsonMessage.get("actuatorId").getAsInt();
        boolean isOn = jsonMessage.get("isOn").getAsBoolean();

        // TODO: Implement the logic to control the actuator based on the received message
        communicationChannel.sendActuatorChange(nodeId, actuatorId, isOn);
    }

    private boolean isShutdownNotification(String serverMessage) {
        return serverMessage.contains("SHUT_DOWN");
    }
}
