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

public class MainControlPanelClass {
    public static void main(String[] args) {
        // Create an instance of the SocketCommunicationChannel
        CommunicationChannel communicationChannel = new SocketCommunicationChannel("localhost", 6019);

        // Open the communication channel
        if (communicationChannel.open()) {
            // Assuming you have an actuator ID, node ID, and a boolean value for isOn
            int actuatorId = 1; // Replace with the actual actuator ID
            int nodeId = 2; // Replace with the actual node ID
            boolean isOn = true; // Replace with the desired value

            // Send a control command to the server
            communicationChannel.sendActuatorChange(nodeId, actuatorId, isOn);
        } else {
            System.out.println("Failed to open communication channel. Exiting...");
        }
    }
}