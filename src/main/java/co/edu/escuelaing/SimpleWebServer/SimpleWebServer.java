package co.edu.escuelaing.SimpleWebServer;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class SimpleWebServer {
    private static final int PORT = 8080;
    public static final String WEB_ROOT = "src/main/java/co/edu/escuelaing/SimpleWebServer/resources";

    public static void main(String[] args) throws IOException {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(new ClientHandler(clientSocket));
            }
        }
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedOutputStream dataOut = new BufferedOutputStream(clientSocket.getOutputStream())) {

            String requestLine = in.readLine();
            if (requestLine == null) return;

            String[] tokens = requestLine.split(" ");
            String method = tokens[0];
            String fileRequested = tokens[1];

            if (method.equals("GET")) {
                handleGetRequest(fileRequested, out, dataOut);
            } else if (method.equals("POST")) {
                handlePostRequest(in, out);
                redirect(out, "/webroot");  // Redirigir a /webroot después de manejar el POST
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleGetRequest(String fileRequested, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        if (fileRequested.equals("/")) {
            fileRequested = "/index.html";
        }
    
        String contentType = FileHandler.getContentType(fileRequested);
        try {
            if (contentType.equals("text/html") && fileRequested.endsWith(".json")) {  // Si es un JSON, mostrar como tabla HTML
                String htmlTable = FileHandler.readJsonAsHtmlTable(fileRequested);
                out.println("HTTP/1.1 200 OK");
                out.println("Content-type: " + contentType);
                out.println("Content-length: " + htmlTable.getBytes().length);  // Calcular longitud correcta
                out.println();
                out.flush();
                out.write(htmlTable);
                out.flush();
            } else if (contentType.startsWith("image/")) {
                byte[] fileData = FileHandler.readImage(fileRequested);
                out.println("HTTP/1.1 200 OK");
                out.println("Content-type: " + contentType);
                out.println("Content-length: " + fileData.length);
                out.println();
                out.flush();
                dataOut.write(fileData, 0, fileData.length);
                dataOut.flush();
            } else if (contentType.equals("text/html")) {
                String fileContent = FileHandler.readHtml(fileRequested);
                out.println("HTTP/1.1 200 OK");
                out.println("Content-type: " + contentType);
                out.println("Content-length: " + fileContent.getBytes().length);  // Calcular longitud correcta
                out.println();
                out.flush();
                out.write(fileContent);
                out.flush();
            } else {
                String fileContent = FileHandler.readText(fileRequested);
                out.println("HTTP/1.1 200 OK");
                out.println("Content-type: " + contentType);
                out.println("Content-length: " + fileContent.getBytes().length);  // Calcular longitud correcta
                out.println();
                out.flush();
                out.write(fileContent);
                out.flush();
            }
        } catch (IOException e) {
            out.println("HTTP/1.1 404 Not Found");
            out.println("Content-type: text/html");
            out.println();
            out.println("<html><body><h1>File Not Found</h1></body></html>");
            out.flush();
        }
    }
    

    private void handlePostRequest(BufferedReader in, PrintWriter out) throws IOException {
        StringBuilder payload = new StringBuilder();
        String line;
        while (!(line = in.readLine()).isEmpty()) {
            payload.append(line).append("\n");
        }

        String response = "{\"message\": \"Data received: " + payload.toString().trim() + "\"}";

        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: application/json");
        out.println();
        out.println(response);
        out.flush();
    }

    private void redirect(PrintWriter out, String location) {
        out.println("HTTP/1.1 302 Found");
        out.println("Location: " + location);
        out.println();
        out.flush();
    }
}
