package no.ntnu.network.client;

import no.ntnu.controlpanel.CommunicationChannel;
import no.ntnu.tools.Logger;
import static no.ntnu.network.server.SmartFarmingServer.PORT;

public class MainSensorActuatorClass {

    public static void main(String[] args) {
        // Check if there are enough command-line arguments
        if (args.length < 6) {
            Logger.error("Usage: java MainSensorActuatorClass <nodeId> <actuatorId> <actuatorType> <isOn> <sensorType> <sensorValue>");
            System.exit(1);
        }

        // Parse command-line arguments
        int nodeId = Integer.parseInt(args[0]);
        int actuatorId = Integer.parseInt(args[1]);
        String actuatorType = (args[2]);
        boolean isOn = Boolean.parseBoolean(args[3]);
        String sensorType = (args[4]);
        double sensorValue = Double.parseDouble(args[5]);

        // Create an instance of the SocketSensorActuatorCommunicationChannel
        CommunicationChannel communicationChannel = new SocketSensorActuatorCommunicationChannel("localhost", PORT);
        if (communicationChannel.open("SENSOR_ACTUATOR")) {
            communicationChannel.sendSensorData(nodeId, actuatorId,actuatorType, isOn, sensorType, sensorValue);
        } else {
            Logger.error("Failed to open communication channel. Exiting....");
        }
    }
}