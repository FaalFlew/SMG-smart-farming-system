package no.ntnu.network.message;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class TrimStringDeserializer implements JsonDeserializer<String> {

    /**
     * Deserializes a JSON element, trimming leading and trailing whitespaces if the element is a string.
     *
     * @param json       The JSON element to be deserialized.
     * @param typeOfT    The type of the object to deserialize to.
     * @param context    The context for deserialization that can be used to delegate part of the process.
     * @return The deserialized object, with leading and trailing whitespaces trimmed if it is a string.
     * @throws JsonParseException If the deserialization encounters issues or fails.
     */
    @Override
    public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            // Trim the string value
            return json.getAsString().trim();
        }
        // If not a string, return as is
        return json.toString();
    }
}