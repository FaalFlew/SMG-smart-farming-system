package no.ntnu.controlpanel;

public class MainControlPanelClass {
    public static void main(String[] args) {
        // Create an instance of the SocketCommunicationChannel
        CommunicationChannel communicationChannel = new SocketCommunicationChannel("localhost", 12345);

        // Create an instance of ControlPanelLogic
        ControlPanelLogic controlPanelLogic = new ControlPanelLogic();
        controlPanelLogic.setCommunicationChannel(communicationChannel);

        //Set communication channel listener if needed
        //controlPanelLogic.setCommunicationChannelListener(...);

        //Add listeners or other necessary setup
        //controlPanelLogic.addListener(...);

        //Open the communication channel
        communicationChannel.open();
    }
}
