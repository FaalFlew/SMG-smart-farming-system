package no.ntnu.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import no.ntnu.tools.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SmartFarmingServer {

    private static final int PORT = 6019;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private static final Gson gson = new Gson();
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

                executorService.execute(() -> handleClient(clientSocket, clientWriter));
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
                JsonObject warningMessage = new JsonObject();
                warningMessage.addProperty("type", "WARNING");
                warningMessage.addProperty("message", "Server will close soon!");

                // Send the warning message to the client
                clientWriter.println(warningMessage.toString());
            } catch (Exception e) {
                Logger.error("Error sending warning to client: " + e.getMessage());
            }
        }
    }
    private static void handleClient(Socket clientSocket,PrintWriter clientWriter) {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            Logger.info("Handling client in thread " + Thread.currentThread().getId());

            String clientMessage;
            while ((clientMessage = reader.readLine()) != null) {
                Logger.info("Received message from client: " + clientMessage);

                // Parse JSON message
                try {
                    Message message = gson.fromJson(clientMessage, Message.class);
                    handleMessage(message, writer);
                } catch (Exception e) {
                    Logger.error("Error parsing message: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            Logger.error("Error handling client: " + e.getMessage());
        } finally {
            try {
                Logger.info("Client disconnected: " + clientSocket.getInetAddress().getHostName() + " [" + clientSocket.getPort() + "]");
                clientSocket.close();
                // Remove the client's PrintWriter from the list when disconnected
                connectedClients.removeIf(writer -> writer.equals(clientWriter));
            } catch (IOException e) {
                Logger.error("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private static void handleMessage(Message message, PrintWriter writer) {
        // TODO: Implement message handling based on the message type
        // This is to integrate the specific smart farming protocol logic

        // For now, just log the received message type
        Logger.info("Received message type: " + message.getType());
    }

    // Message class for demonstration purposes
    private static class Message {
        private String type;

        public String getType() {
            return type;
        }
    }
}
