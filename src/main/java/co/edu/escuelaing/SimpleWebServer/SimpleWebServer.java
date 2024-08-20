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
            if (contentType.equals("application/json")) {
                System.out.println("Document: " + fileRequested);
                System.out.println("File Not Found");
                String htmlTable = FileHandler.readJsonAsHtmlTable(fileRequested);
                out.println("HTTP/1.1 200 OK");
                out.println("Content-type: " + contentType);
                out.println("Content-length: " + htmlTable.length());
                out.println();
                out.flush();
                out.print(htmlTable);
                out.flush();
            } else if (contentType.startsWith("image/")) {
                System.out.println("Document: " + fileRequested);
                System.out.println("File Not Found");
                byte[] fileData = FileHandler.readImage(fileRequested);
                out.println("HTTP/1.1 200 OK");
                out.println("Content-type: " + contentType);
                out.println("Content-length: " + fileData.length);
                out.println();
                out.flush();
                dataOut.write(fileData, 0, fileData.length);
                dataOut.flush();
            } else if (contentType.equals("text/html")) {
                System.out.println("Document: " + fileRequested);
                System.out.println("File Not Found");
                String fileContent = FileHandler.readHtml(fileRequested);
                out.println("HTTP/1.1 200 OK");
                out.println("Content-type: " + contentType);
                out.println("Content-length: " + fileContent.length());
                out.println();
                out.flush();
                out.print(fileContent);
                out.flush();
            } else {
                System.out.println("Document: " + fileRequested);
                System.out.println("File Not Found");
                String fileContent = FileHandler.readText(fileRequested);
                out.println("HTTP/1.1 200 OK");
                out.println("Content-type: " + contentType);
                out.println("Content-length: " + fileContent.length());
                out.println();
                out.flush();
                out.print(fileContent);
                out.flush();
            }
        } catch (IOException e) {
            System.out.println("Document: " + fileRequested);
            System.out.println("File Not Found");
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

    private String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".html")) return "text/html";
        else if (fileRequested.endsWith(".css")) return "text/css";
        else if (fileRequested.endsWith(".js")) return "application/javascript";
        else if (fileRequested.endsWith(".png")) return "image/png";
        else if (fileRequested.endsWith(".jpg")) return "image/jpeg";
        return "text/plain";
    }

    private byte[] readFileData(File file, int fileLength) throws IOException {
        byte[] fileData = new byte[fileLength];
        try (FileInputStream fileIn = new FileInputStream(file)) {
            fileIn.read(fileData);
        }
        return fileData;
    }
}
