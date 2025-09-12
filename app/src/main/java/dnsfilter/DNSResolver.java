package dnsfilter;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class DNSResolver {

    private static final int MAX_THREADS = 10;
    private static final ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
    private final DNSFilterManager filterManager;

    public DNSResolver(DNSFilterManager filterManager) {
        this.filterManager = filterManager;
    }

    public void handleQuery(byte[] queryData, InetAddress clientAddress) {
        executor.submit(() -> {
            try {
                SimpleDNSMessage message = new SimpleDNSMessage(queryData, clientAddress);
                String domain = message.getQueriedDomain();

                if (domain == null) {
                    System.err.println("[DNSResolver] Truy vấn không hợp lệ từ " + clientAddress.getHostAddress());
                    return;
                }

                boolean isBlocked = filterManager.isBlocked(domain);
                message.setBlocked(isBlocked);

                if (isBlocked) {
                    DNSResponsePatcher.logBlockedQuery(domain, clientAddress);
                    byte[] response = DNSResponsePatcher.createBlockedResponse(domain, clientAddress);
                    sendResponse(response, clientAddress);
                } else {
                    byte[] response = forwardToUpstreamDNS(queryData);
                    sendResponse(response, clientAddress);
                }

            } catch (Exception e) {
                System.err.println("[DNSResolver] Lỗi xử lý truy vấn: " + e.getMessage());
            }
        });
    }

    private byte[] forwardToUpstreamDNS(byte[] queryData) {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetSocketAddress dnsServer = DNSServer.getSafeDNSServer("1.1.1.1", 53);
            if (dnsServer == null) return new byte[0];

            DatagramPacket request = new DatagramPacket(queryData, queryData.length, dnsServer.getAddress(), dnsServer.getPort());
            socket.send(request);

            byte[] buffer = new byte[512];
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            socket.setSoTimeout(3000);
            socket.receive(response);

            return response.getData();
        } catch (IOException e) {
            System.err.println("[DNSResolver] Lỗi khi chuyển tiếp truy vấn: " + e.getMessage());
            return new byte[0];
        }
    }

    private void sendResponse(byte[] responseData, InetAddress clientAddress) {
        try (DatagramSocket socket = new DatagramSocket()) {
            DatagramPacket response = new DatagramPacket(responseData, responseData.length, clientAddress, 53);
            socket.send(response);
        } catch (IOException e) {
            System.err.println("[DNSResolver] Lỗi khi gửi phản hồi: " + e.getMessage());
        }
    }

    public void shutdown() {
        executor.shutdown();
        System.out.println("[DNSResolver] Đã dừng xử lý truy vấn.");
    }
}
