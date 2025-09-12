package dnsfilter;

import java.util.*;

public class ConfigUtil {

    /**
     * Kiểm tra xem chuỗi có phải là giá trị boolean hợp lệ
     */
    public static boolean isValidBoolean(String value) {
        if (value == null) return false;
        String v = value.trim().toLowerCase();
        return v.equals("true") || v.equals("false");
    }

    /**
     * Chuyển chuỗi thành boolean an toàn
     */
    public static boolean parseBoolean(String value, boolean defaultValue) {
        if (isValidBoolean(value)) {
            return Boolean.parseBoolean(value.trim());
        }
        return defaultValue;
    }

    /**
     * Kiểm tra xem chuỗi có phải là số nguyên hợp lệ
     */
    public static boolean isValidInteger(String value) {
        if (value == null) return false;
        try {
            Integer.parseInt(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Chuyển chuỗi thành số nguyên an toàn
     */
    public static int parseInteger(String value, int defaultValue) {
        if (isValidInteger(value)) {
            return Integer.parseInt(value.trim());
        }
        return defaultValue;
    }

    /**
     * Kiểm tra key cấu hình có hợp lệ không
     */
    public static boolean isValidConfigKey(String key) {
        return key != null && key.matches("^[a-zA-Z0-9_.-]+$");
    }

    /**
     * Kiểm tra giá trị cấu hình có hợp lệ không
     */
    public static boolean isValidConfigValue(String value) {
        return value != null && value.length() <= 256;
    }

    /**
     * Lọc danh sách cấu hình để loại bỏ key/value không hợp lệ
     */
    public static Map<String, String> sanitizeConfig(Map<String, String> rawConfig) {
        Map<String, String> safeConfig = new HashMap<>();
        for (Map.Entry<String, String> entry : rawConfig.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (isValidConfigKey(key) && isValidConfigValue(value)) {
                safeConfig.put(key.trim(), value.trim());
            }
        }
        return safeConfig;
    }
}
