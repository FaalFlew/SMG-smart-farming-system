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

    @Override
    public void sendActuatorChange(int nodeId, int actuatorId, boolean isOn) {
        String message = String.format("{\"type\":\"ACTUATOR_CONTROL\",\"nodeId\":%d,\"actuatorId\":%d,\"isOn\":%b}",
                nodeId, actuatorId, isOn);
        if (writer != null) {
            writer.println(message);
        }
    }

    @Override
    public boolean open() {
        try {
            Socket socket = new Socket(serverAddress, serverPort);
            writer = new PrintWriter(socket.getOutputStream(), true);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}