package no.ntnu.server;

import no.ntnu.greenhouse.SensorReading;
import no.ntnu.server.message.MessageHandler;
import no.ntnu.tools.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import no.ntnu.greenhouse.Actuator;

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
                String warningMessage = MessageHandler.createWarningMessage("Server will close soon!");

                // Send the warning message to the client
                clientWriter.println(warningMessage);
            } catch (Exception e) {
                Logger.error("Error sending warning to client: " + e.getMessage());
            }
        }
    }

    private static void handleClient(Socket clientSocket, PrintWriter clientWriter) {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
            Logger.info("Handling client in thread " + Thread.currentThread().getId());

            String clientMessage;
            while ((clientMessage = reader.readLine()) != null) {
                Logger.info("Received message from client: " + clientMessage);
                // Handle the message
                handleMessage(clientMessage, clientWriter);
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

    private static void handleMessage(String clientMessage, PrintWriter writer) {
        // Parse JSON message and delegate to MessageHandler
        try {
            String messageType = MessageHandler.getMessageType(clientMessage);
            switch (messageType) {
                case "SENSOR_DATA":
                    handleSensorData(clientMessage, writer);
                    break;
                case "ACTUATOR_CONTROL":
                    handleActuatorControl(clientMessage, writer);
                    break;
                case "WARNING":
                    handleWarning(clientMessage, writer);
                    break;
                // Add more cases for other message types

                default:
                    Logger.error("Unknown message type: " + messageType);
            }
        } catch (Exception e) {
            Logger.error("Error handling message: " + e.getMessage());
        }
    }

    private static void handleSensorData(String clientMessage, PrintWriter writer) {
        // Parse JSON message and perform specific handling
        List<SensorReading> sensorDataList = MessageHandler.parseSensorDataMessage(clientMessage);

        // TODO: Implement sensor data handling logic

        // Respond to the client if needed
        // Example: Acknowledge receipt with a success message
        String response = MessageHandler.createSuccessResponse("SENSOR_DATA");
        writer.println(response);
    }

    private static void handleActuatorControl(String clientMessage, PrintWriter writer) {
        // Parse JSON message and perform specific handling
        List<Actuator> actuatorStatusList = MessageHandler.parseActuatorStatusMessage(clientMessage);

        // TODO: Implement actuator control logic

        // Respond to the client if needed
        // Example: Acknowledge receipt with a success message
        String response = MessageHandler.createSuccessResponse("ACTUATOR_CONTROL");
        writer.println(response);
    }

    private static void handleWarning(String clientMessage, PrintWriter writer) {
        // Handle the warning message
        Logger.warning("Received warning from client: " + clientMessage);

        // Acknowledge receipt with a success message
        String response = MessageHandler.createSuccessResponse("WARNING");
        writer.println(response);
    }
}
