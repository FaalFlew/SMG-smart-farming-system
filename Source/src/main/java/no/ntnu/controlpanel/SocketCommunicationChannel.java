package no.ntnu.controlpanel;

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
        // TODO: change to JSON format
        String message = String.format("{\"type\":\"ACTUATOR_CONTROL\",\"nodeId\":%d,\"actuatorId\":%d,\"isOn\":%b}",
                nodeId, actuatorId, isOn);
        if (writer != null) {
            writer.println(message);
        }
    }

    public boolean open() {
        Socket socket = null;

        try {
            socket = new Socket(serverAddress, serverPort);
            writer = new PrintWriter(socket.getOutputStream(), true);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            // Close the socket if it was opened
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}