package no.ntnu.network.client;

import no.ntnu.tools.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientListener implements Runnable {

    private final SocketCommunicationChannel communicationChannel;

    public ClientListener(SocketCommunicationChannel communicationChannel) {
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

    private boolean isShutdownNotification(String serverMessage) {
        return serverMessage.contains("SHUT_DOWN");
    }
}