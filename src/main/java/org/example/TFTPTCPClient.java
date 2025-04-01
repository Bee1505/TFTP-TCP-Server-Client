package org.example;

import java.io.*;
import java.net.*;

/**
 * TFTP-TCP Client
 * Connects to the server to either send (write) or receive (read) files.
 */
public class TFTPTCPClient {
    private static final int SERVER_PORT = 6969;

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java TFTPTCPClient <server-ip> <operation> <filename>");
            System.out.println("Operations: read | write");
            return;
        }

        String serverIp = args[0].trim(); // Read IP from argument
        String operation = args[1].trim().toLowerCase(); // Normalize operation
        String filename = args[2].trim();

        System.out.println("Connecting to server at: " + serverIp);
        System.out.println("Operation: " + operation);
        System.out.println("Filename: " + filename);

        try (Socket socket = new Socket(serverIp, SERVER_PORT);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            // Send request to server
            out.writeUTF(operation);
            out.writeUTF(filename);

            if (operation.equals("read")) {
                receiveFile(in, filename);
            } else if (operation.equals("write")) {
                sendFile(out, filename);
            } else {
                System.out.println("Invalid operation. Use 'read' or 'write'.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void receiveFile(DataInputStream in, String filename) throws IOException {
        String serverResponse = in.readUTF();
        if (serverResponse.startsWith("ERROR")) {
            System.out.println("Server error: " + serverResponse);
            return;
        }

        try (FileOutputStream fos = new FileOutputStream("received_" + filename)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
        System.out.println("File received successfully: received_" + filename);
    }

    private static void sendFile(DataOutputStream out, String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            out.writeUTF("ERROR: File not found");
            System.out.println("File not found: " + filename);
            return;
        }
        out.writeUTF("OK");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        System.out.println("File sent successfully: " + filename);
    }

}
