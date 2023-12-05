package no.ntnu.network.client;

import no.ntnu.tools.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientListener implements Runnable {

    private final Socket clientSocket;

    public ClientListener(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String serverMessage;
            while ((serverMessage = reader.readLine()) != null) {
                Logger.info("Received message from server: " + serverMessage);

                // TODO: Handle the server message as needed

                // Example
                // messagehandler.handleServerMessage(serverMessage);
            }
        } catch (IOException e) {
            Logger.error("Error reading server message: " + e.getMessage());
        }
    }
}