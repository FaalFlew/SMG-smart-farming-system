package no.ntnu.controlpanel;

import java.net.Socket;

public interface ExtendedCommunicationChannel extends CommunicationChannel {

    void setShutdownReceived(boolean shutdownReceived);

    void closeSocketAndWriter();

    Socket getClientSocket();
}