package no.ntnu.network.client.clientinfo;

import java.io.PrintWriter;

public class ControlPanelClientInfo extends BaseClientInfo {
    public ControlPanelClientInfo(int nodeId, String clientAddress, int clientPort, PrintWriter clientWriter) {
        super(nodeId, clientAddress, clientPort, clientWriter);
    }
}