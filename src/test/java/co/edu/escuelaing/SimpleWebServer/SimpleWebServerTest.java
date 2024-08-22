package co.edu.escuelaing.SimpleWebServer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.Socket;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link SimpleWebServer} class.
 * These tests verify that the SimpleWebServer can start and accept connections.
 */
public class SimpleWebServerTest {

    /**
     * Tests that the SimpleWebServer starts correctly and accepts connections.
     * <p>
     * This test runs the server in a separate thread to prevent blocking the test execution.
     * It then waits for 2 seconds to ensure the server has time to start up.
     * After the wait, it attempts to connect to the server on port 8080 and verifies
     * that the connection is successful, indicating that the server is running.
     * </p>
     */
    @Test
    public void testServerStarts() {
        // Start the server in a separate thread
        Thread serverThread = new Thread(() -> {
            try {
                SimpleWebServer.main(new String[]{});
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        
        serverThread.start();
        
        // Give the server some time to start
        try {
            Thread.sleep(2000); // Wait for 2 seconds to ensure the server is up
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Check if the server is running by attempting to connect to it
        try (Socket socket = new Socket("localhost", 8080)) {
            assertTrue(socket.isConnected(), "Server should be running and accepting connections.");
        } catch (IOException e) {
            fail("Server failed to start or accept connections.");
        }
    }
}
