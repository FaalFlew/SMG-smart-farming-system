package no.ntnu.network.client.clientinfo;

import java.io.PrintWriter;

public class BaseClientInfo {
    private final int nodeId;
    private final String clientAddress;
    private final int clientPort;
    private final PrintWriter clientWriter;

    public BaseClientInfo(int nodeId, String clientAddress, int clientPort, PrintWriter clientWriter) {
        this.nodeId = nodeId;
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        this.clientWriter = clientWriter;
    }

    public int getNodeId() {
        return nodeId;
    }

    public String getClientAddress() {
        return clientAddress;
    }
    public int getClientPort() {
        return clientPort;
    }

    public PrintWriter getClientWriter() {
        return clientWriter;
    }
}