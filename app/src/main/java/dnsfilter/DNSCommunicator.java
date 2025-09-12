package dnsfilter;

import java.io.*;
import java.net.*;
import javax.net.ssl.*;

public class DNSCommunicator {

    private static final int TIMEOUT_MS = 3000;

    /**
     * Gửi truy vấn DNS qua UDP đến server tin cậy
     */
    public static byte[] sendUDPQuery(byte[] queryData, String dnsIp, int port) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);

            InetSocketAddress dnsServer = DNSServer.getSafeDNSServer(dnsIp, port);
            if (dnsServer == null) return new byte[0];

            DatagramPacket request = new DatagramPacket(queryData, queryData.length, dnsServer.getAddress(), dnsServer.getPort());
            socket.send(request);

            byte[] buffer = new byte[512];
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            socket.receive(response);

            return response.getData();
        } catch (IOException e) {
            System.err.println("[DNSCommunicator] Lỗi UDP: " + e.getMessage());
            return new byte[0];
        }
    }

    /**
     * Gửi truy vấn DNS qua TLS (DoT)
     */
    public static byte[] sendDoTQuery(byte[] queryData, String host, int port) {
        try {
            SSLSocket socket = DNSServer.createSecureDoTConnection(host, port);
            if (socket == null) return new byte[0];

            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            // Gửi độ dài + dữ liệu
            out.write((queryData.length >> 8) & 0xFF);
            out.write(queryData.length & 0xFF);
            out.write(queryData);
            out.flush();

            // Nhận độ dài phản hồi
            int len1 = in.read();
            int len2 = in.read();
            int responseLength = (len1 << 8) | len2;

            if (responseLength <= 0 || responseLength > 512) {
                System.err.println("[DNSCommunicator] Phản hồi DoT không hợp lệ.");
                socket.close();
                return new byte[0];
            }

            byte[] response = new byte[responseLength];
            int read = in.read(response);
            socket.close();

            return (read == responseLength) ? response : new byte[0];

        } catch (IOException e) {
            System.err.println("[DNSCommunicator] Lỗi DoT: " + e.getMessage());
            return new byte[0];
        }
    }
}
