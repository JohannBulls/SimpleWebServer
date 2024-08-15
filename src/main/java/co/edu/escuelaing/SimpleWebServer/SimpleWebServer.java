package co.edu.escuelaing.SimpleWebServer;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * A simple multi-threaded web server that listens for incoming connections
 * and serves static files from a specified root directory.
 */
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

/**
 * A handler class that processes client requests. This class is responsible
 * for reading the HTTP request, serving the requested file, and sending the
 * appropriate HTTP response back to the client.
 */
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
            
            // Print request header for debugging
            printRequestHeader(in);

            String[] tokens = requestLine.split(" ");
            String method = tokens[0];
            String fileRequested = tokens[1];

            if (method.equals("GET")) {
                handleGetRequest(fileRequested, out, dataOut);
            } else if (method.equals("POST")) {
                handlePostRequest(in, out);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printRequestHeader(BufferedReader in) throws IOException {
        System.out.println("Request headers:");
        String line;
        while (!(line = in.readLine()).isEmpty()) {
            System.out.println("Received: " + line);
        }
    }

    private void handleGetRequest(String fileRequested, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        File file = new File(SimpleWebServer.WEB_ROOT, fileRequested);
        if (file.exists()) {
            int fileLength = (int) file.length();
            String contentType = getContentType(fileRequested);
            byte[] fileData = readFileData(file, fileLength);

            out.println("HTTP/1.1 200 OK");
            out.println("Content-type: " + contentType);
            out.println("Content-length: " + fileLength);
            out.println();
            out.flush();
            dataOut.write(fileData, 0, fileLength);
            dataOut.flush();
        } else {
            out.println("HTTP/1.1 404 Not Found");
            out.println("Content-type: text/html");
            out.println();
            out.flush();
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

    private String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".html")) return "text/html";
        else if (fileRequested.endsWith(".css")) return "text/css";
        else if (fileRequested.endsWith(".js")) return "application/javascript";
        else if (fileRequested.endsWith(".png")) return "image/png";
        else if (fileRequested.endsWith(".jpg")) return "image/jpeg";
        return "text/plain";
    }

    private byte[] readFileData(File file, int fileLength) throws IOException {
        try (FileInputStream fileIn = new FileInputStream(file)) {
            byte[] fileData = new byte[fileLength];
            fileIn.read(fileData);
            return fileData;
        }
    }
}
