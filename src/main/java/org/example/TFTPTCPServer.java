package org.example;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TFTP-TCP Server
 * Supports multiple clients, handling file read and write requests over TCP.
 * Ensures proper error handling and multi-threading for concurrent transfers.
 */

public class TFTPTCPServer {
    private static final int SERVER_PORT = 6969;
    private static final int THREAD_POOL_SIZE = 10; // Supports up to 10 clients simultaneously

    public static void main(String[] args) {
        System.out.println("Server is running from: " + new File(".").getAbsolutePath());
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("TFTP TCP Server is running on port " + SERVER_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                executor.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }
}

/**
 * Handles individual client requests.
 * Supports "read" (file download) and "write" (file upload) operations.
 */

class ClientHandler implements Runnable {
    private final Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (DataInputStream input = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream())) {

            String requestType = input.readUTF(); // Expecting "read" or "write"
            String filename = input.readUTF();
            System.out.println("Request: " + requestType + " | Filename: " + filename);

            if ("read".equalsIgnoreCase(requestType)) {
                sendFile(filename, output);
            } else if ("write".equalsIgnoreCase(requestType)) {
                receiveFile(filename, input);
            } else {
                output.writeUTF("ERROR: Invalid request type");
                System.out.println("Invalid request type received.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendFile(String filename, DataOutputStream output) throws IOException {
        File file = new File("server_files", filename);
        if (!file.exists()) {
            output.writeUTF("ERROR: File not found");
            System.out.println("File not found: " + filename);
            return;
        }

        output.writeUTF("OK");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }
        System.out.println("File " + filename + " sent successfully.");
    }

    private void receiveFile(String filename, DataInputStream input) throws IOException {
        File directory = new File("server_files");  // Define the folder
        if (!directory.exists()) {
            directory.mkdir();  // Create it if it doesn't exist
        }

        File file = new File(directory, filename);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
        System.out.println("File " + filename + " received successfully.");
    }
}