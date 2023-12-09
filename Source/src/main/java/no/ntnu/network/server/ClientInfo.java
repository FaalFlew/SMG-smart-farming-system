package no.ntnu.network.server;

import java.io.PrintWriter;

public class ClientInfo {
    private final int nodeId;
    private final int actuatorId;
    private final boolean isOn;
    private final String clientAddress;
    private final PrintWriter clientWriter;

    public ClientInfo(int nodeId, int actuatorId, boolean isOn, String clientAddress, PrintWriter clientWriter) {
        this.nodeId = nodeId;
        this.actuatorId = actuatorId;
        this.isOn = isOn;
        this.clientAddress = clientAddress;
        this.clientWriter = clientWriter;
    }

    public int getNodeId() {
        return nodeId;
    }

    public int getActuatorId() {
        return actuatorId;
    }

    public boolean isOn() {
        return isOn;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public PrintWriter getClientWriter() {
        return clientWriter;
    }

}
