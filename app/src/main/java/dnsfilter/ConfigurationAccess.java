package dnsfilter;

import java.io.*;
import java.util.*;

public class ConfigurationAccess {

    private static final String CONFIG_FILE = "/data/data/dnsfilter/config.properties";
    private final Properties config = new Properties();

    public ConfigurationAccess() {
        loadConfig();
    }

    /**
     * Tải cấu hình từ file
     */
    public void loadConfig() {
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            config.clear();
            config.load(fis);
            System.out.println("[ConfigurationAccess] Đã tải cấu hình.");
        } catch (IOException e) {
            System.err.println("[ConfigurationAccess] Lỗi khi tải cấu hình: " + e.getMessage());
        }
    }

    /**
     * Lấy giá trị cấu hình theo key
     */
    public String get(String key) {
        if (key == null || key.trim().isEmpty()) return null;
        return config.getProperty(key.trim());
    }

    /**
     * Ghi giá trị cấu hình (có xác thực đơn giản)
     */
    public boolean set(String key, String value, String authToken) {
        if (!isAuthorized(authToken)) {
            System.err.println("[ConfigurationAccess] Truy cập bị từ chối.");
            return false;
        }

        if (!isValidKey(key) || !isValidValue(value)) {
            System.err.println("[ConfigurationAccess] Dữ liệu cấu hình không hợp lệ.");
            return false;
        }

        config.setProperty(key.trim(), value.trim());
        return saveConfig();
    }

    /**
     * Lưu cấu hình ra file
     */
    private boolean saveConfig() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            config.store(fos, "PersonalDNSFilter Configuration");
            System.out.println("[ConfigurationAccess] Đã lưu cấu hình.");
            return true;
        } catch (IOException e) {
            System.err.println("[ConfigurationAccess] Lỗi khi lưu cấu hình: " + e.getMessage());
            return false;
        }
    }

    /**
     * Xác thực đơn giản bằng token
     */
    private boolean isAuthorized(String token) {
        return "AzSecureToken123".equals(token); // Bạn có thể thay đổi token này
    }

    /**
     * Kiểm tra key hợp lệ
     */
    private boolean isValidKey(String key) {
        return key != null && key.matches("^[a-zA-Z0-9_.-]+$");
    }

    /**
     * Kiểm tra value hợp lệ
     */
    private boolean isValidValue(String value) {
        return value != null && value.length() <= 256;
    }
}
