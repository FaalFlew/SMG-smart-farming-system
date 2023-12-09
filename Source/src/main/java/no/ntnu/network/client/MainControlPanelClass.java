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
     * @param args The command-line arguments (provide different values for actuatorId, nodeId, and isOn).
*/
    public static void main(String[] args) {
        // Check if there are enough command-line arguments
        if (args.length < 3) {
            // args like 3 1 true
            Logger.error("Usage: java MainControlPanelClass <actuatorId> <nodeId> <isOn>");
            System.exit(1);
        }
        // Parse command-line arguments
        int actuatorId = Integer.parseInt(args[0]);
        int nodeId = Integer.parseInt(args[1]);
        boolean isOn = Boolean.parseBoolean(args[2]);

        // Create an instance of the SocketCommunicationChannel
        CommunicationChannel communicationChannel = new SocketCommunicationChannel("localhost", PORT);
        if (communicationChannel.open("CONTROL_PANEL")) {
            communicationChannel.sendActuatorChange(nodeId, actuatorId, isOn);
        } else {
            Logger.error("Failed to open communication channel. Exiting....");
        }
    }
}

/**
package no.ntnu.network.client;

import no.ntnu.controlpanel.CommunicationChannel;
import no.ntnu.network.client.SocketCommunicationChannel;
import no.ntnu.tools.Logger;

import static no.ntnu.network.server.SmartFarmingServer.PORT;

public class MainControlPanelClass {

    private static int lastNodeId = 1;

    public static void main(String[] args) {
        // Create an instance of the SocketCommunicationChannel
        CommunicationChannel communicationChannel = new SocketCommunicationChannel("localhost", PORT);
        if (communicationChannel.open("CONTROL_PANEL")) {
            int actuatorId = 1;
            int nodeId = getNextNodeId();
            boolean isOn = true;
            communicationChannel.sendActuatorChange(nodeId, actuatorId, isOn);

        } else {
            Logger.error("Failed to open communication channel. Exiting...");
        }
    }

    private static int getNextNodeId() {
        return lastNodeId++;
    }
}*/