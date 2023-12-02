package no.ntnu.server.message;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import no.ntnu.greenhouse.Actuator;
import no.ntnu.greenhouse.SensorReading;
import no.ntnu.controlpanel.ControlPanelLogic;

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
