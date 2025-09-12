package dnsfilter;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;

public class BlockedHosts {

    private static final String BLOCKED_HOSTS_FILE = "/data/data/dnsfilter/FILTERHOSTS.txt";
    private final Set<String> blockedHosts = new HashSet<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public BlockedHosts() {
        load();
    }

    /**
     * Tải danh sách host bị chặn từ file
     */
    public void load() {
        lock.writeLock().lock();
        try (BufferedReader reader = new BufferedReader(new FileReader(BLOCKED_HOSTS_FILE))) {
            blockedHosts.clear();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim().toLowerCase();
                if (!line.isEmpty() && !line.startsWith("#") && isValidDomain(line)) {
                    blockedHosts.add(line);
                }
            }
            System.out.println("[BlockedHosts] Đã tải " + blockedHosts.size() + " host bị chặn.");
        } catch (IOException e) {
            System.err.println("[BlockedHosts] Lỗi khi tải danh sách host: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Kiểm tra xem một tên miền có bị chặn không
     */
    public boolean isBlocked(String domain) {
        lock.readLock().lock();
        try {
            return blockedHosts.contains(domain.toLowerCase());
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Trả về danh sách host bị chặn dưới dạng chuỗi
     */
    public String getAllBlockedHosts() {
        lock.readLock().lock();
        try {
            if (blockedHosts.isEmpty()) return "Không có host nào bị chặn.";
            StringBuilder sb = new StringBuilder();
            for (String host : blockedHosts) {
                sb.append(host).append("\n");
            }
            return sb.toString();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Kiểm tra định dạng tên miền
     */
    private boolean isValidDomain(String domain) {
        return domain.matches("^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }
}
