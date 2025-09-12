package dnsfilter;

import java.net.*;
import java.util.*;
import javax.net.ssl.*;

public class DNSServer {

    // Danh sách DNS server tin cậy
    private static final List<String> TRUSTED_DNS = Arrays.asList(
        "1.1.1.1", "8.8.8.8", "9.9.9.9"
    );

    /**
     * Trả về DNS server nếu nằm trong danh sách tin cậy
     */
    public static InetSocketAddress getSafeDNSServer(String ip, int port) {
        if (!TRUSTED_DNS.contains(ip)) {
            System.err.println("[DNSServer] DNS server không nằm trong danh sách tin cậy: " + ip);
            return null;
        }
        return new InetSocketAddress(ip, port);
    }

    /**
     * Tạo kết nối DNS-over-TLS (DoT) an toàn
     */
    public static SSLSocket createSecureDoTConnection(String host, int port) {
        try {
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
            socket.setSoTimeout(5000);
            socket.startHandshake();

            SSLSession session = socket.getSession();
            String peerHost = session.getPeerHost();

            if (!host.equalsIgnoreCase(peerHost)) {
                System.err.println("[DNSServer] Hostname không khớp trong chứng chỉ SSL: " + peerHost);
                socket.close();
                return null;
            }

            return socket;
        } catch (Exception e) {
            System.err.println("[DNSServer] Lỗi khi tạo kết nối DoT: " + e.getMessage());
            return null;
        }
    }

    // Có thể mở rộng thêm cho DoH (DNS over HTTPS) nếu cần
}
