package no.ntnu.network.message;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import no.ntnu.greenhouse.Actuator;
import no.ntnu.greenhouse.SensorReading;
import no.ntnu.controlpanel.ControlPanelLogic;
import no.ntnu.tools.Logger;

import java.util.List;

/**
 * The MessageHandler class provides utility methods for creating and handling JSON messages related to Smart Farming communication
 */
public class MessageHandler {

    private static final Gson gson = new Gson();

    /**
     * Creates a JSON message for sensor data with the specified node ID and list of sensor readings
     *
     * @param nodeId         The ID of the node sending the sensor data.
     * @param sensorDataList The list of sensor readings to be included in the message.
     * @return The JSON message representing sensor data.
     */
    public static String createSensorDataMessage(int nodeId, List<SensorReading> sensorDataList) {
        JsonObject message = new JsonObject();
        message.addProperty("type", "SENSOR_DATA");
        message.addProperty("nodeId", nodeId);

        // Create a JSON array for sensor data
        JsonArray sensorDataArray = new JsonArray();
        for (SensorReading sensorReading : sensorDataList) {
            sensorDataArray.add(gson.toJsonTree(sensorReading));
        }
        message.add("sensorData", sensorDataArray);

        return message.toString();
    }

    /**
     * Creates a JSON message for actuator status with the specified node ID and list of actuators
     *
     * @param nodeId             The ID of the node providing actuator status.
     * @param actuatorStatusList The list of actuators and their statuses to be included in the message.
     * @return The JSON message representing actuator status.
     */
    public static String createActuatorStatusMessage(int nodeId, List<Actuator> actuatorStatusList) {
        JsonObject message = new JsonObject();
        message.addProperty("type", "ACTUATOR_STATUS");
        message.addProperty("nodeId", nodeId);

        // Create a JSON array for actuator status
        JsonArray actuatorStatusArray = new JsonArray();
        for (Actuator actuator : actuatorStatusList) {
            actuatorStatusArray.add(gson.toJsonTree(actuator));
        }
        message.add("actuatorStatus", actuatorStatusArray);

        return message.toString();
    }

    /**
     * Creates a JSON message for control commands with the specified control panel node ID and list of commands.
     *
     * @param controlPanelNodeId The ID of the control panel node sending the commands.
     * @param controlCommands    The list of control commands to be included in the message
     * @return The JSON message representing control commands.
     */
    public static String createControlCommandMessage(int controlPanelNodeId, List<ControlPanelLogic> controlCommands) {
        JsonObject message = new JsonObject();
        message.addProperty("type", "CONTROL_COMMAND");
        message.addProperty("controlPanelNodeId", controlPanelNodeId);

        // Create a JSON array for control commands
        JsonArray controlCommandArray = new JsonArray();
        for (ControlPanelLogic controlCommand : controlCommands) {
            controlCommandArray.add(gson.toJsonTree(controlCommand));
        }
        message.add("controlCommands", controlCommandArray);

        return message.toString();
    }

    /**
     * Creates a warning message with the specified content.
     *
     * @param message The content of the warning message.
     * @return The JSON message representing a warning
     */
    public static String createWarningMessage(String message) {
        // Placeholder method for creating a warning message
        JsonObject warningMessage = new JsonObject();
        warningMessage.addProperty("type", "WARNING");
        warningMessage.addProperty("message", message);
        return warningMessage.toString();
    }

    /**
     * Creates a warning message with the specified content.
     *
     * @param message The content of the warning message.
     * @return The JSON message representing a warning
     */
    public static String createShutdownMessage(String message) {
        // Placeholder method for creating a warning message
        JsonObject shutdownMessage = new JsonObject();
        shutdownMessage.addProperty("type", "SHUT_DOWN");
        shutdownMessage.addProperty("message", message);
        return shutdownMessage.toString();
    }

    /**
     * Extracts and returns the type of the message from the provided JSON message.
     *
     * @param message The JSON message from which to extract the type.
     * @return The type of the message as a String
     * @throws IllegalArgumentException If there is an error parsing the JSON or if the message does not contain a type attribute.
     */
    public static String getMessageType(String message) {
        try {
            if (message == null) {
                Logger.error("Input message is null.");
            }

            JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);

            if (jsonMessage == null) {
                Logger.error("Invalid JSON format. Unable to parse the message.");
            }

            if (!jsonMessage.has("type")) {
                Logger.error( "Message does not contain a 'type' attribute");
            }

            return jsonMessage.get("type").getAsString();
        } catch (JsonSyntaxException e) {
            String errorMessage = "Error parsing JSON: " + e.getMessage();
            Logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }


    /**
     * Creates a success response message with the specified content
     *
     * @param message The content of the success response
     * @return The JSON message representing a success response.
     */
    public static String createSuccessResponse(String message) {
        // Placeholder method for creating a success response
        JsonObject successResponse = new JsonObject();
        successResponse.addProperty("type", "SUCCESS");
        successResponse.addProperty("message", message);
        return successResponse.toString();
    }

    /**
     * Parses a JSON sensor data message and returns the list of sensor readings.
     *
     * @param message The JSON message containing sensor data.
     * @return The list of sensor readings parsed from the message
     */
    public static List<SensorReading> parseSensorDataMessage(String message) {
        JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);
        return gson.fromJson(jsonMessage.getAsJsonArray("sensorData"), List.class);
    }

    /**
     * Parses a JSON actuator status message and returns the list of actuators
     *
     * @param message The JSON message containing actuator status
     * @return The list of actuators parsed from the message
     */
    public static List<Actuator> parseActuatorStatusMessage(String message) {
        JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);
        return gson.fromJson(jsonMessage.getAsJsonArray("actuatorStatus"), List.class);
    }

    /**
     * Parses a JSON control command message and returns the list of control commands.
     *
     * @param message The JSON message containing control commands
     * @return The list of control commands parsed from the message.
     */
    public static List<ControlPanelLogic> parseControlCommandMessage(String message) {
        JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);
        return gson.fromJson(jsonMessage.getAsJsonArray("controlCommands"), List.class);
    }
}
