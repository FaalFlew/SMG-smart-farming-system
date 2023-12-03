package no.ntnu.controlpanel;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketCommunicationChannel implements CommunicationChannel {
    private final String serverAddress;

    private final int serverPort;
    private PrintWriter writer;

    public SocketCommunicationChannel(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    /**
     * Sends a control command to change the state of an actuator.
     *
     * @param nodeId     The unique identifier of the node associated with the actuator
     * @param actuatorId The identifier of the actuator to be controlled.
     * @param isOn       A boolean whether to turn the actuator on or off.
     */
    @Override
    public void sendActuatorChange(int nodeId, int actuatorId, boolean isOn) {
        JsonObject jsonMessage = new JsonObject();
        jsonMessage.addProperty("type", "ACTUATOR_CONTROL");
        jsonMessage.addProperty("nodeId", nodeId);
        jsonMessage.addProperty("actuatorId", actuatorId);
        jsonMessage.addProperty("isOn", isOn);


        //String message = String.format("{\"type\":\"ACTUATOR_CONTROL\",\"nodeId\":%d,\"actuatorId\":%d,\"isOn\":%b}",
        // nodeId, actuatorId, isOn);
        String message = jsonMessage.toString();
        if (writer != null) {
            writer.println(message);
        }
    }

    public boolean open() {
        Socket socket = createSocket();

        if (socket != null) {
            boolean initialized = initializeWriter(socket);
            if (initialized) {
                // Keep the application running in a loop
                while (true) {
                    // You can add your logic or wait for user input here
                    // For example, waiting for the user to press a key
                    System.out.println("Press any key to exit.");
                    try {
                        System.in.read();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                closeSocket(socket);
            }
        }
        return false;
    }
    private Socket createSocket() {
        try {
            return new Socket(serverAddress, serverPort);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean initializeWriter(Socket socket) {
        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void closeSocket(Socket socket) {
        if (socket != null && !socket.isClosed()) {
            try {
                if (writer != null) {
                    writer.close();
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void sleepMilliseconds(int delayInMilliseconds) {
        try {
            Thread.sleep(delayInMilliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}