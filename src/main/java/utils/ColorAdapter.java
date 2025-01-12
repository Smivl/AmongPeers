package utils;

import com.google.gson.*;
import javafx.scene.paint.Color;
import org.jspace.io.json.jSonUtils;

import java.lang.reflect.Type;

// Custom serializer for Color
public class ColorAdapter implements JsonSerializer<Color>, JsonDeserializer<Color> {

    public static void init() {
        ColorAdapter colorAdapter = new ColorAdapter();
        jSonUtils utils = jSonUtils.getInstance();
        utils.register("pspace:color" ,Color.class ,colorAdapter , colorAdapter);
    }

    @Override
    public JsonElement serialize(Color color, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("red", color.getRed());
        json.addProperty("green", color.getGreen());
        json.addProperty("blue", color.getBlue());
        json.addProperty("opacity", color.getOpacity());
        return json;
    }

    @Override
    public Color deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        double red = jsonObject.get("red").getAsDouble();
        double green = jsonObject.get("green").getAsDouble();
        double blue = jsonObject.get("blue").getAsDouble();
        double opacity = jsonObject.get("opacity").getAsDouble();
        return new Color(red, green, blue, opacity);
    }
}
