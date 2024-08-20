package co.edu.escuelaing.SimpleWebServer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.nio.file.Files;
import java.util.Map;

public class FileHandler {
    private static final String WEB_ROOT = "src/main/java/co/edu/escuelaing/SimpleWebServer/resources";

    // Lee un archivo JSON y lo convierte a una tabla HTML
    public static String readJsonAsHtmlTable(String fileName) throws IOException {
        String jsonString = readFile(fileName);
        JsonElement jsonElement = JsonParser.parseString(jsonString);
        return convertJsonToHtmlTable(jsonElement);
    }

    // Lee un archivo de texto y lo devuelve como un String
    public static String readText(String fileName) throws IOException {
        return readFile(fileName);
    }

    // Lee una imagen y la devuelve como un array de bytes
    public static byte[] readImage(String fileName) throws IOException {
        File file = new File(WEB_ROOT, fileName);
        return Files.readAllBytes(file.toPath());
    }

    // Lee una página HTML y la devuelve como un String
    public static String readHtml(String fileName) throws IOException {
        return readFile(fileName);
    }

    // Lee cualquier tipo de archivo y lo devuelve como un String
    private static String readFile(String fileName) throws IOException {
        File file = new File(WEB_ROOT, fileName);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line).append("\n");
        }
        reader.close();
        return content.toString();
    }

    // Determina el tipo de contenido basado en la extensión del archivo
    public static String getContentType(String fileName) {
        if (fileName.endsWith(".html")) return "text/html";
        else if (fileName.endsWith(".css")) return "text/css";
        else if (fileName.endsWith(".js")) return "application/javascript";
        else if (fileName.endsWith(".json")) return "text/html";  // Cambiado a text/html para que se muestre la tabla correctamente
        else if (fileName.endsWith(".png")) return "image/png";
        else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        return "text/plain";
    }

    // Convierte un JsonElement a una tabla HTML
    private static String convertJsonToHtmlTable(JsonElement jsonElement) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body><table border='1'>");

        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            html.append("<tr><th>Key</th><th>Value</th></tr>");
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                html.append("<tr>")
                    .append("<td>").append(entry.getKey()).append("</td>")
                    .append("<td>").append(formatJsonValue(entry.getValue())).append("</td>")
                    .append("</tr>");
            }
        } else if (jsonElement.isJsonArray()) {
            html.append("<tr><th>Index</th><th>Value</th></tr>");
            for (int i = 0; i < jsonElement.getAsJsonArray().size(); i++) {
                JsonElement element = jsonElement.getAsJsonArray().get(i);
                html.append("<tr>")
                    .append("<td>").append(i).append("</td>")
                    .append("<td>").append(formatJsonValue(element)).append("</td>")
                    .append("</tr>");
            }
        }

        html.append("</table></body></html>");
        return html.toString();
    }

    // Formatea un valor JSON para la tabla HTML
    private static String formatJsonValue(JsonElement value) {
        if (value.isJsonObject()) {
            return "[Object]";
        } else if (value.isJsonArray()) {
            return "[Array]";
        } else if (value.isJsonPrimitive()) {
            return value.getAsJsonPrimitive().toString();
        }
        return "";
    }
}
