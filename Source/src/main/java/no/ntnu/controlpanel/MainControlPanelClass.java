/*package no.ntnu.controlpanel;

public class MainControlPanelClass {
    public static void main(String[] args) {
        // Create an instance of the SocketCommunicationChannel
        CommunicationChannel communicationChannel = new SocketCommunicationChannel("localhost", 6019);

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
*/

package no.ntnu.controlpanel;

import no.ntnu.tools.Logger;

import static no.ntnu.server.SmartFarmingServer.PORT;
public class MainControlPanelClass {
    public static void main(String[] args) {
        // Create an instance of the SocketCommunicationChannel
        CommunicationChannel communicationChannel = new SocketCommunicationChannel("localhost", PORT);
        if (communicationChannel.open("CONTROL_PANEL")) {
            int actuatorId = 1;
            int nodeId = 2;
            boolean isOn = true;
            communicationChannel.sendActuatorChange(nodeId, actuatorId, isOn);

        } else {
            Logger.error("Failed to open communication channel. Exiting...");
        }
    }
}