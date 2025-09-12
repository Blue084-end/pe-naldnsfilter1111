package dnsfilter;

import java.io.*;
import java.net.*;
import java.util.regex.*;

public class SimpleDNSMessage {

    private static final Pattern DOMAIN_PATTERN = Pattern.compile("^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private String queriedDomain;
    private InetAddress clientAddress;
    private boolean isBlocked;

    public SimpleDNSMessage(byte[] data, InetAddress clientAddress) {
        this.clientAddress = clientAddress;
        parseQuery(data);
    }

    private void parseQuery(byte[] data) {
        try {
            // Giả định: domain bắt đầu từ byte thứ 12
            int index = 12;
            StringBuilder domainBuilder = new StringBuilder();

            while (index < data.length && data[index] != 0) {
                int length = data[index++] & 0xFF;
                if (index + length > data.length) break;

                domainBuilder.append(new String(data, index, length)).append(".");
                index += length;
            }

            String domain = domainBuilder.toString();
            if (domain.endsWith(".")) domain = domain.substring(0, domain.length() - 1);

            if (DOMAIN_PATTERN.matcher(domain).matches()) {
                this.queriedDomain = domain.toLowerCase();
            } else {
                System.err.println("[SimpleDNSMessage] Tên miền không hợp lệ: " + domain);
                this.queriedDomain = null;
            }

        } catch (Exception e) {
            System.err.println("[SimpleDNSMessage] Lỗi khi phân tích gói DNS: " + e.getMessage());
            this.queriedDomain = null;
        }
    }

    public String getQueriedDomain() {
        return queriedDomain;
    }

    public InetAddress getClientAddress() {
        return clientAddress;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        this.isBlocked = blocked;
    }

    public byte[] createResponse() {
        if (queriedDomain == null) return new byte[0];

        // Tạo phản hồi DNS đơn giản (giả lập)
        ByteArrayOutputStream response = new ByteArrayOutputStream();
        try {
            response.write("Blocked response for ".getBytes());
            response.write(queriedDomain.getBytes());
        } catch (IOException e) {
            System.err.println("[SimpleDNSMessage] Lỗi khi tạo phản hồi: " + e.getMessage());
        }
        return response.toByteArray();
    }
}
