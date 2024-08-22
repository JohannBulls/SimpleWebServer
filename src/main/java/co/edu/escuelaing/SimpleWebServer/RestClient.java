package co.edu.escuelaing.SimpleWebServer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;

/**
 * A client for making asynchronous HTTP requests.
 * This class uses Java's HttpClient to send GET requests and handle responses.
 */
public class RestClient {

    private static HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Sets the HttpClient to be used for sending requests.
     *
     * @param client The HttpClient to set.
     */
    public static void setHttpClient(HttpClient client) {
        httpClient = client;
    }

    /**
     * Sends an asynchronous HTTP GET request to the specified URL.
     * The request is sent using the HttpClient and the response is handled asynchronously.
     *
     * @param url The URL to which the GET request is sent.
     * @return A CompletableFuture containing the response body as a String. 
     *         If an error occurs, the future will contain an error message.
     */
    public static CompletableFuture<String> sendAsyncGetRequest(String url) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        return httpClient.sendAsync(request, BodyHandlers.ofString())
            .thenApply(HttpResponse::body)
            .exceptionally(ex -> "Error: " + ex.getMessage());
    }
}
