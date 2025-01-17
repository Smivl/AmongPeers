import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class testingServer {
    private static final int DISCOVERY_PORT = 9876;  // pick a port not commonly used
    private static final String DISCOVERY_REQUEST = "DISCOVERY_REQUEST";
    private static final String DISCOVERY_RESPONSE = "SERVER_AVAILABLE:MyGameServer";
    private boolean running = true;

    public void startServer() {
        try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT)) {
            System.out.println("Server listening for discovery requests on port " + DISCOVERY_PORT);

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

                    byte[] responseBuf = DISCOVERY_RESPONSE.getBytes();
                    DatagramPacket responsePacket = new DatagramPacket(responseBuf,
                            responseBuf.length,
                            clientAddress,
                            clientPort);
                    socket.send(responsePacket);

                    System.out.println("Responded to client: " + clientAddress + ":" + clientPort);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        running = false;
    }

    public static void main(String[] args) {
        new testingServer().startServer();
    }
}
