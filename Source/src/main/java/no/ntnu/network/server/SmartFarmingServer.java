package no.ntnu.network.server;

import no.ntnu.network.message.MessageHandler;
import no.ntnu.tools.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
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
                Logger.info("Client connected: " + clientSocket.getInetAddress().getHostName() +
                        " [" + clientSocket.getPort() + "]");

                // Store the client's PrintWriter in the list
                PrintWriter clientWriter = new PrintWriter(clientSocket.getOutputStream(), true);
                connectedClients.add(clientWriter);

                // Identify client type
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String clientType = getClientType(reader);

                // Create a new instance of ClientHandler with client type
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
     * Reads and returns the client type from the provided BufferedReader
     *
     * @param reader The BufferedReader connected to the client's input stream
     * @return The client type as a String
     * @throws IOException If an I/O error occurs while reading the client type
     */
    private static String getClientType(BufferedReader reader) throws IOException {
        return reader.readLine();
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


}