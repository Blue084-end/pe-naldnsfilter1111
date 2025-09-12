package dnsfilter;

import java.io.IOException;
import java.net.*;

public class DNSFilterProxy {

    private static final int LISTEN_PORT = 53;
    private static final int MAX_PACKET_SIZE = 512;
    private final DNSResolver resolver;

    public DNSFilterProxy(DNSResolver resolver) {
        this.resolver = resolver;
    }

    public void start() {
        try (DatagramSocket socket = new DatagramSocket(LISTEN_PORT)) {
            System.out.println("[DNSFilterProxy] Đang lắng nghe tại cổng " + LISTEN_PORT);

            byte[] buffer = new byte[MAX_PACKET_SIZE];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();

                // Chỉ cho phép truy vấn từ localhost
                if (!clientAddress.isLoopbackAddress()) {
                    System.err.println("[DNSFilterProxy] Từ chối truy vấn từ IP không hợp lệ: " + clientAddress.getHostAddress());
                    continue;
                }

                byte[] queryData = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), 0, queryData, 0, packet.getLength());

                resolver.handleQuery(queryData, clientAddress);
            }

        } catch (IOException e) {
            System.err.println("[DNSFilterProxy] Lỗi khi lắng nghe truy vấn DNS: " + e.getMessage());
        }
    }
}
