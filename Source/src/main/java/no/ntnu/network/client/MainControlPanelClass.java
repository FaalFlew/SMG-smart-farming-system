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

package no.ntnu.network.client;

import no.ntnu.controlpanel.CommunicationChannel;
import no.ntnu.tools.Logger;


import static no.ntnu.network.server.SmartFarmingServer.PORT;
/**
 * The MainControlPanelClass serves as the entry point for the Control Panel application
 * It demonstrates the usage of the SocketCommunicationChannel and sends a sample actuator control command
 */
public class MainControlPanelClass {

    /**
     * The main method that gets executed when running the Control Panel application.
     *
     * @param args The command-line arguments (not used in this implementation).
     */
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