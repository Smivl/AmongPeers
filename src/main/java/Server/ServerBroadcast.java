package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Dictionary;

public class ServerBroadcast {

    private static String IP;
    private static int DISCOVERY_PORT = 9000;  // pick a port not commonly used
    private static final String DISCOVERY_REQUEST = "DISCOVERY_REQUEST";
    private static boolean running = true;

    public static String getIPAddress(){
        try {
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.connect(InetAddress.getByName("230.0.0.1"), 9000);
                IP = socket.getLocalAddress().getHostAddress();
                return IP;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int setupServerPort(){
        // check if server exists on this port otherwise increment
        while(ServerScan.serverExistsOnPort(DISCOVERY_PORT, IP)) DISCOVERY_PORT++;
        return DISCOVERY_PORT;
    }

    public static void startServer(String serverName) {

        try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT)) {
            System.out.println("Server listening for discovery requests on port " + DISCOVERY_PORT);
            running = true;

            String responseMsg = "SERVER_AVAILABLE:" + serverName + "SERVER_IP:" + getIPAddress();

            while (running) {
                // Prepare packet buffer
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                // Receive packet
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());

                if (received.equals(DISCOVERY_REQUEST)) {
                    // Client is discovering servers, send a response
                    InetAddress clientAddress = packet.getAddress();
                    int clientPort = packet.getPort();


                    byte[] responseBuf = responseMsg.getBytes();
                    DatagramPacket responsePacket = new DatagramPacket(responseBuf,
                            responseBuf.length,
                            clientAddress,
                            clientPort);
                    socket.send(responsePacket);

                    System.out.println("Responded to client: " + clientAddress + ":" + clientPort);
                }
            }
        } catch (IOException e) {
            System.out.println("ERROR HERE");
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }
    public static void stopServer() {
        running = false;
    }

    public static void main(String[] args){
        ServerBroadcast.startServer("nick's lobby");
    }

}
