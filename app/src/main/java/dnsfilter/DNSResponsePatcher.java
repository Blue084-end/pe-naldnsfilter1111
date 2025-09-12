package dnsfilter;

import java.io.*;
import java.net.*;

public class DNSResponsePatcher {

    private static final int MAX_BUFFER_SIZE = 512;

    /**
     * Tạo phản hồi DNS bị chặn cho tên miền đã lọc
     */
    public static byte[] createBlockedResponse(String queriedDomain, InetAddress clientAddress) {
        if (queriedDomain == null || queriedDomain.isEmpty()) {
            System.err.println("[DNSResponsePatcher] Tên miền không hợp lệ.");
            return new byte[0];
        }

        ByteArrayOutputStream response = new ByteArrayOutputStream();

        try {
            String message = "Domain " + queriedDomain + " is blocked.";
            byte[] messageBytes = message.getBytes();

            if (messageBytes.length > MAX_BUFFER_SIZE) {
                System.err.println("[DNSResponsePatcher] Phản hồi quá lớn, bị cắt bớt.");
                response.write(messageBytes, 0, MAX_BUFFER_SIZE);
            } else {
                response.write(messageBytes);
            }

        } catch (IOException e) {
            System.err.println("[DNSResponsePatcher] Lỗi khi tạo phản hồi: " + e.getMessage());
        }

        return response.toByteArray();
    }

    /**
     * Ghi log truy vấn bị chặn (chỉ khi bật chế độ debug)
     */
    public static void logBlockedQuery(String domain, InetAddress clientAddress) {
        if (domain == null || clientAddress == null) return;

        // Chỉ ghi log nếu cần (có thể kiểm tra biến DEBUG nếu bạn dùng)
        System.out.println("[DNSResponsePatcher] Đã chặn truy vấn: " + domain + " từ " + clientAddress.getHostAddress());
    }
}
