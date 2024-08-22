package co.edu.escuelaing.SimpleWebServer;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * A simple web server implementation in Java that handles both GET and POST requests.
 * The server serves static files and supports asynchronous communication with REST services.
 */
public class SimpleWebServer {
    private static final int PORT = 8080;
    public static final String WEB_ROOT = "src/main/java/co/edu/escuelaing/SimpleWebServer/resources/webroot";

    /**
     * The main method to start the web server. It creates a thread pool to handle multiple
     * client connections concurrently and listens on the specified port.
     *
     * @param args Command line arguments (not used).
     * @throws IOException if an I/O error occurs when opening the socket.
     */
    public static void main(String[] args) throws IOException {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(new ClientHandler(clientSocket));
            }
        }
    }
}

/**
 * A Runnable class that handles the client's requests. This class processes
 * GET and POST HTTP requests and sends appropriate responses back to the client.
 */
class ClientHandler implements Runnable {
    private Socket clientSocket;

    /**
     * Constructs a ClientHandler to manage the communication with a specific client.
     *
     * @param socket The socket connected to the client.
     */
    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    /**
     * The main logic of the ClientHandler. It reads the HTTP request, determines
     * whether it is a GET or POST request, and calls the appropriate method to handle it.
     */
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

            // Print the request method and route to the console
            System.out.println("Method: " + method + ", Route: " + fileRequested);

            if (method.equals("GET")) {
                handleGetRequest(fileRequested, out, dataOut);
            } else if (method.equals("POST")) {
                handlePostRequest(in, out);
                redirect(out, "/index.html");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles HTTP GET requests. It serves static files or interacts with an asynchronous
     * REST service depending on the requested path.
     *
     * @param fileRequested The requested file or resource path.
     * @param out The PrintWriter to send the response header.
     * @param dataOut The BufferedOutputStream to send the file data.
     * @throws IOException if an I/O error occurs while handling the request.
     */
    private void handleGetRequest(String fileRequested, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        if (fileRequested.equals("/")) {
            fileRequested = "/index.html";
        }

        String filePath;
        if (fileRequested.equals("/index.html") || fileRequested.equals("/style.css") || fileRequested.equals("/script.js")) {
            filePath = "src/main/java/co/edu/escuelaing/SimpleWebServer/resources" + fileRequested;
        } else if (fileRequested.equals("/api/data")) {
            // Asynchronous communication with a REST service
            RestClient.sendAsyncGetRequest("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=fb&apikey=Q1QZFVJQ21K7C6XM")
                .thenAccept(response -> {
                    out.println("HTTP/1.1 200 OK");
                    out.println("Content-Type: application/json");
                    out.println("Content-Length: " + response.length());
                    out.println();
                    out.println(response);
                    out.flush();
                });
            return; 
        } else {
            filePath = SimpleWebServer.WEB_ROOT + fileRequested;
        }

        File file = new File(filePath);
        String contentType = getContentType(fileRequested);

        if (file.exists()) {
            byte[] fileData = readFileData(file);
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: " + contentType);
            out.println("Content-Length: " + fileData.length);
            out.println();
            out.flush();
            dataOut.write(fileData, 0, fileData.length);
            dataOut.flush();
        } else {
            out.println("HTTP/1.1 404 Not Found");
            out.println("Content-Type: text/html");
            out.println();
            out.println("<html><body><h1>File Not Found</h1></body></html>");
            out.flush();
        }
    }

    /**
     * Handles HTTP POST requests. It reads the payload and returns it in the response.
     *
     * @param in The BufferedReader to read the request body.
     * @param out The PrintWriter to send the response.
     * @throws IOException if an I/O error occurs while handling the request.
     */
    private void handlePostRequest(BufferedReader in, PrintWriter out) throws IOException {
        StringBuilder payload = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            payload.append(line);
        }

        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: text/html");
        out.println();
        out.println("<html><body><h1>POST data received:</h1>");
        out.println("<p>" + payload.toString() + "</p>");
        out.println("</body></html>");
        out.flush();
    }

    /**
     * Sends an HTTP redirect to the client.
     *
     * @param out The PrintWriter to send the redirect response.
     * @param location The new location to which the client should be redirected.
     */
    private void redirect(PrintWriter out, String location) {
        out.println("HTTP/1.1 302 Found");
        out.println("Location: " + location);
        out.println();
        out.flush();
    }

    /**
     * Determines the content type of the requested file based on its extension.
     *
     * @param fileRequested The requested file path.
     * @return The content type as a String.
     */
    private String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".html")) {
            return "text/html";
        } else if (fileRequested.endsWith(".css")) {
            return "text/css";
        } else if (fileRequested.endsWith(".js")) {
            return "application/javascript";
        } else if (fileRequested.endsWith(".png")) {
            return "image/png";
        } else if (fileRequested.endsWith(".jpg")) {
            return "image/jpeg";
        } else {
            return "text/plain";
        }
    }

    /**
     * Reads the file data into a byte array.
     *
     * @param file The file to be read.
     * @return A byte array containing the file data.
     * @throws IOException if an I/O error occurs while reading the file.
     */
    private byte[] readFileData(File file) throws IOException {
        try (FileInputStream fileIn = new FileInputStream(file)) {
            byte[] fileData = new byte[(int) file.length()];
            fileIn.read(fileData);
            return fileData;
        }
    }
}
