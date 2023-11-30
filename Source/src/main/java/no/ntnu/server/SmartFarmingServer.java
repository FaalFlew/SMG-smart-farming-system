package no.ntnu.server;

import com.google.gson.Gson;
import no.ntnu.tools.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SmartFarmingServer {

    private static final int PORT = 6019;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            Logger.info("Smart Farming Server started, waiting for client connections...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                Logger.info("Client connected: " + clientSocket.getInetAddress().getHostName() +
                        " [" + clientSocket.getPort() + "]");

                executorService.execute(() -> handleClient(clientSocket));
            }

        } catch (IOException e) {
            Logger.error("Error starting the server: " + e.getMessage());
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            Logger.info("Handling client in thread " + Thread.currentThread().getId());

            String clientMessage;
            while ((clientMessage = reader.readLine()) != null) {
                Logger.info("Received message from client: " + clientMessage);

                //Parses JSON message
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
                clientSocket.close();
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
