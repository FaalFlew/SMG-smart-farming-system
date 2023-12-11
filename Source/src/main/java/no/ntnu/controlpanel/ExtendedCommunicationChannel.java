package no.ntnu.controlpanel;

import java.net.Socket;

public interface ExtendedCommunicationChannel extends CommunicationChannel {

    void sendGetSensorData(int nodeId, long timer);

    void setShutdownReceived(boolean shutdownReceived);

    void closeSocketAndWriter();

    Socket getClientSocket();
}