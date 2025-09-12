package ip;

public class CheckSum {

    /**
     * Tính toán checksum cho một đoạn dữ liệu
     */
    public static int chkSum(byte[] buf, int off, int cnt) {
        int sum = 0;
        for (int i = 0; i < cnt; i += 2) {
            int val = (buf[off + i] & 0xFF) << 8;
            if (i + 1 < cnt) val += (buf[off + i + 1] & 0xFF);
            sum += val;
        }

        while ((sum >> 16) != 0) {
            sum = (sum & 0xFFFF) + (sum >> 16);
        }

        return (~sum) & 0xFFFF;
    }
}
