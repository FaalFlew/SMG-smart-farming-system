package no.ntnu.server.message;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import no.ntnu.greenhouse.Actuator;
import no.ntnu.greenhouse.SensorReading;
import no.ntnu.controlpanel.ControlPanelLogic;
import no.ntnu.tools.Logger;

import java.util.List;

public class MessageHandler {

    private static final Gson gson = new Gson();

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
    public static String createWarningMessage(String message) {
        // Placeholder method for creating a warning message
        JsonObject warningMessage = new JsonObject();
        warningMessage.addProperty("type", "WARNING");
        warningMessage.addProperty("message", message);
        return warningMessage.toString();
    }

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


    public static String createSuccessResponse(String message) {
        // Placeholder method for creating a success response
        JsonObject successResponse = new JsonObject();
        successResponse.addProperty("type", "SUCCESS");
        successResponse.addProperty("message", message);
        return successResponse.toString();
    }
    public static List<SensorReading> parseSensorDataMessage(String message) {
        JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);
        return gson.fromJson(jsonMessage.getAsJsonArray("sensorData"), List.class);
    }

    public static List<Actuator> parseActuatorStatusMessage(String message) {
        JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);
        return gson.fromJson(jsonMessage.getAsJsonArray("actuatorStatus"), List.class);
    }

    public static List<ControlPanelLogic> parseControlCommandMessage(String message) {
        JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);
        return gson.fromJson(jsonMessage.getAsJsonArray("controlCommands"), List.class);
    }
}
