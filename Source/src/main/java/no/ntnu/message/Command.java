package no.ntnu.message;

import com.google.gson.JsonObject;

public class Command {
    private String targetNodeId;
    private CommandType commandType;
    private JsonObject commandDetails;

    public Command(String targetNodeId, CommandType commandType, JsonObject commandDetails) {
        this.targetNodeId = targetNodeId;
        this.commandType = commandType;
        this.commandDetails = commandDetails;
    }

    public String getTargetNodeId() {
        return targetNodeId;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public JsonObject getCommandDetails() {
        return commandDetails;
    }

    // Additional methods for JSON serialization/deserialization if needed

    // Enum to represent different types of commands
    public enum CommandType {
        TURN_ON_FAN,
        TURN_OFF_FAN,
        TURN_ON_HEATER,
        TURN_OFF_HEATER,
        OPEN_WINDOW,
        CLOSE_WINDOW,
    }
}
