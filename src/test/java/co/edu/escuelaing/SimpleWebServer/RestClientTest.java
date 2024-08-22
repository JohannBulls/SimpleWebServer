package co.edu.escuelaing.SimpleWebServer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for the {@link RestClient} class.
 * These tests verify the behavior of the RestClient when sending asynchronous HTTP GET requests.
 */
public class RestClientTest {

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private HttpResponse<String> mockResponse;

    /**
     * Initializes the test environment before each test.
     * Sets up mocks and configures the RestClient to use the mock HttpClient.
     */
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        RestClient.setHttpClient(mockHttpClient); // Assuming you add a setter for HttpClient in RestClient
    }

    /**
     * Tests the {@link RestClient#sendAsyncGetRequest(String)} method for a successful HTTP request.
     * <p>
     * This test simulates a successful HTTP GET request by returning a mock HttpResponse
     * with a predefined response body. It verifies that the method returns the expected response body.
     * </p>
     * 
     * @throws Exception if an error occurs during the test
     */
    @Test
    public void testSendAsyncGetRequest_Success() throws Exception {
        // Arrange
        String url = "https://api.example.com/data";
        String responseBody = "{\"key\":\"value\"}";
        CompletableFuture<HttpResponse<String>> futureResponse = CompletableFuture.completedFuture(mockResponse);
        
        when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(futureResponse);
        when(mockResponse.body()).thenReturn(responseBody);

        // Act
        CompletableFuture<String> resultFuture = RestClient.sendAsyncGetRequest(url);
        String result = resultFuture.get();

        // Assert
        assertEquals(responseBody, result);
    }

    /**
     * Tests the {@link RestClient#sendAsyncGetRequest(String)} method for a failed HTTP request.
     * <p>
     * This test simulates a failed HTTP GET request by returning a CompletableFuture
     * that completes exceptionally with a RuntimeException. It verifies that the method
     * returns an error message prefixed with "Error:".
     * </p>
     * 
     * @throws Exception if an error occurs during the test
     */
    @Test
    public void testSendAsyncGetRequest_Failure() throws Exception {
        // Arrange
        String url = "https://api.example.com/data";
        CompletableFuture<HttpResponse<String>> futureResponse = CompletableFuture.failedFuture(new RuntimeException("Network error"));

        when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(futureResponse);

        // Act
        CompletableFuture<String> resultFuture = RestClient.sendAsyncGetRequest(url);
        String result = resultFuture.get();

        // Assert
        assertTrue(result.startsWith("Error:"));
    }
}
