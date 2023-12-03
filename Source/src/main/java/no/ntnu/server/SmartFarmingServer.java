// SmartFarmingServer.java
package no.ntnu.server;

import no.ntnu.server.message.MessageHandler;
import no.ntnu.tools.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SmartFarmingServer {

    private static final int PORT = 6019;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private static final List<PrintWriter> connectedClients = new ArrayList<>();

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Perform cleanup tasks, including sending the warning message
            sendWarningToAllClients();
            executorService.shutdown();
        }));
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            Logger.info("Smart Farming Server started, waiting for client connections...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                Logger.info("Client connected: " + clientSocket.getInetAddress().getHostName() +
                        " [" + clientSocket.getPort() + "]");

                // Store the client's PrintWriter in the list
                PrintWriter clientWriter = new PrintWriter(clientSocket.getOutputStream(), true);
                connectedClients.add(clientWriter);

                // Create a new instance of ClientHandler for each client
                executorService.execute(new ClientHandler(clientSocket, clientWriter));
            }

        } catch (IOException e) {
            Logger.error("Error starting the server: " + e.getMessage());
        } finally {
            // Call the method to send a warning to all connected clients before shutting down
            sendWarningToAllClients();

            // Perform any other cleanup or shutdown tasks here
            executorService.shutdown();
        }
    }

    private static void sendWarningToAllClients() {
        for (PrintWriter clientWriter : connectedClients) {
            try {
                // Create a warning message
                String warningMessage = MessageHandler.createWarningMessage("Server will close soon!");

                // Send the warning message to the client
                clientWriter.println(warningMessage);
            } catch (Exception e) {
                Logger.error("Error sending warning to client: " + e.getMessage());
            }
        }
    }
}
