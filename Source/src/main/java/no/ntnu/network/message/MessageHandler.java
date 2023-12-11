package no.ntnu.network.message;

import com.google.gson.*;
import no.ntnu.greenhouse.Actuator;
import no.ntnu.greenhouse.SensorReading;
import no.ntnu.controlpanel.ControlPanelLogic;
import no.ntnu.tools.Logger;

import java.io.PrintWriter;
import java.util.List;

/**
 * The MessageHandler class provides utility methods for creating and handling JSON messages related to Smart Farming communication
 */
public class MessageHandler {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(String.class, new TrimStringDeserializer())
            .create();

    /**
     * Validates the format of a JSON message string by attempting to parse it into a JsonObject.
     * The method performs the following validations:
     * 1. Converts the entire message to lowercase.
     * 2. Removes all spaces from the message.
     * 3. Attempts to parse the modified message into a JsonObject using Gson.
     *
     * @param userInput The input JSON message string to be validated.
     * @return The validated and modified JSON message string.
     * @throws IllegalArgumentException If the provided message is not a valid JSON format.
     */
    public static String validateMessageFormat(String userInput) {
        try {
            // Turn all message to lowercase
            userInput = userInput.toLowerCase();

            // remove all spaces from the message
            userInput = userInput.replaceAll("\\s", "");
            gson.fromJson(userInput, JsonObject.class);
            return userInput;
        } catch (JsonSyntaxException e) {
            Logger.error("Error parsing JSON: " + e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
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
                Logger.error("Message does not contain a 'type' attribute");
            }

            return jsonMessage.get("type").getAsString();
        } catch (JsonSyntaxException e) {
            String errorMessage = "Error parsing JSON: " + e.getMessage();
            Logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Sends the list of available commands to the control panel client
     *
     * @param writer The PrintWriter used for sending messages to the client
     */
    public static void sendAvailableCommandsListControlPanel(PrintWriter writer) {
        // Create a JSON object containing the list of available commands
        JsonObject commandList = new JsonObject();
        commandList.addProperty("type", "available_commands");

        // Add the available commands based on your application
        // For example:
        JsonArray commandsArray = new JsonArray();
        commandsArray.add("ALL_CONTROL_PANELS");
        commandsArray.add("ALL_SENSORS");
        commandsArray.add("COMMAND_TO_SENSOR_ACTUATOR");
        // Add more commands as needed

        commandList.add("commands", commandsArray);

        // Send the available commands to the client
        writer.println(commandList.toString());
    }
    /**
     *
     * Sends the list of available commands to the sensor actuator node client
     *
     * @param writer The PrintWriter used for sending messages to the client
     */
    public static void sendAvailableCommandsListSensorActuator(PrintWriter writer) {
        // Create a JSON object containing the list of available commands
        JsonObject commandList = new JsonObject();
        commandList.addProperty("type", "available_commands");

        // the available commands
        JsonArray commandsArray = new JsonArray();
        commandsArray.add("ALL_SENSORS");

        commandList.add("commands", commandsArray);

        // Send the available commands to the client
        writer.println(commandList.toString());
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
