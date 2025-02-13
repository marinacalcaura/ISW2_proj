package deliverable.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class JSON {
    private JSON(){
        throw new IllegalStateException("Utility class");
    }

    private static String readAll(Reader reader) throws IOException {
        StringBuilder content = new StringBuilder();
        int character;
        while ((character = reader.read()) != -1) {
            content.append((char) character);
        }
        return content.toString();
    }

    public static JSONArray JsonArrayFromUrl(String url) throws IOException, JSONException {
        try (InputStream inputStream = new URL(url).openStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return new JSONArray(readAll(reader));
        }
    }

    public static JSONObject JsonObjectFromUrl(String url) throws IOException, JSONException {
        try (InputStream inputStream = new URL(url).openStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return new JSONObject(readAll(reader));
        }
    }
}
