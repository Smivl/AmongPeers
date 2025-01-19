package Server;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;


public class ServerScan {
    private static final int DISCOVERY_PORT = 9876;
    private static final String DISCOVERY_REQUEST = "DISCOVERY_REQUEST";

    public static Map<String, DatagramPacket> scanForServers() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(3000);

            Map<String, DatagramPacket> serverIPs = new HashMap<>() {
            };

            // Broadcast address: 255.255.255.255 (universal) or your subnet broadcast (e.g., 192.168.1.255)
            InetAddress broadcastAddress = InetAddress.getByName("192.168.0.255");

            byte[] sendData = DISCOVERY_REQUEST.getBytes();
            for (int i = 0; i < 50; i++) {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcastAddress, DISCOVERY_PORT + i);
                socket.send(sendPacket);
            }
            //DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcastAddress, DISCOVERY_PORT);
            //socket.send(sendPacket);
            System.out.println(InetAddress.getAllByName(InetAddress.getLocalHost().getHostName())[0].getHostAddress());
            System.out.println("Discovery request sent to broadcast...");

            // Listen for responses
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 3000) { // 3-second window
                try {
                    byte[] buffer = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                    socket.receive(receivePacket);

                    String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    System.out.println("Received: " + response + " from "
                            + receivePacket.getAddress() + ":" + receivePacket.getPort());

                    serverIPs.put(response, receivePacket);

                    if (response.startsWith("SERVER_AVAILABLE:")) {
                        // Parse server info, store or display it in the UI
                        String serverInfo = response.substring("SERVER_AVAILABLE:".length());
                        // e.g., add to a list of discovered servers
                    }
                } catch (SocketTimeoutException e) {
                    // Timeout while waiting for next response - just loop again
                }
            }

            System.out.println("Finished scanning.");

            return serverIPs;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean serverExistsOnPort(int port, String ip) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(200);

            // Broadcast address: 255.255.255.255 (universal) or your subnet broadcast (e.g., 192.168.1.255)
            InetAddress broadcastAddress = InetAddress.getByName("192.168.0.255");

            byte[] sendData = DISCOVERY_REQUEST.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcastAddress, port);
            socket.send(sendPacket);

            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 200) { // 3-second window
                try {
                    byte[] buffer = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                    socket.receive(receivePacket);

                    String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    System.out.println("Received: " + response + " from "
                            + receivePacket.getAddress() + ":" + receivePacket.getPort());

                    if(receivePacket.getAddress().toString().replace("/", "").equals(ip)) return true;

                } catch (SocketTimeoutException e) {
                    // Timeout while waiting for next response - just loop again
                }
            }
            return false;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}