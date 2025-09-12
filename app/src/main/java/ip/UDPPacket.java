package ip;

import java.nio.*;

public class UDPPacket extends IPPacket {

    private final IntBuffer udpHeader;

    public UDPPacket(byte[] packet, int offs, int len) {
        super(packet, offs, len);
        this.udpHeader = ByteBuffer.wrap(packet, offs + ipHdrlen, 8).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
    }

    public static UDPPacket createUDPPacket(byte[] packet, int offs, int len, int version) {
        packet[offs] = (byte) ((version << 4) & 0xF0);
        UDPPacket udp = new UDPPacket(packet, offs, len);
        udp.initInitialIPHeader();
        return udp;
    }

    public void updateHeader(int sourcePort, int destPort) {
        if (!isValidPort(sourcePort) || !isValidPort(destPort)) {
            System.err.println("[UDPPacket] Cổng không hợp lệ: " + sourcePort + " hoặc " + destPort);
            return;
        }

        int[] hdr = new int[2];
        hdr[0] = (sourcePort << 16) + destPort;
        hdr[1] = (len - ipHdrlen) << 16;
        udpHeader.position(0);
        udpHeader.put(hdr);

        int checksum = calculateCheckSum(true);
        udpHeader.put(1, hdr[1] + checksum);
    }

    public int checkCheckSum() {
        return calculateCheckSum(false);
    }

    private int calculateCheckSum(boolean internal) {
        int checkSum = 0;
        if (version == 4) {
            int saved = ipHeader.get(2);
            ipHeader.put(2, (17 << 16) + len - ipHdrlen);
            checkSum = CheckSum.chkSum(data, offset + ipHdrlen, len - ipHdrlen);
            ipHeader.put(2, saved);
        } else if (version == 6) {
            int[] saved = new int[]{ipHeader.get(0), ipHeader.get(1)};
            ipHeader.put(0, len - ipHdrlen);
            ipHeader.put(1, 17);
            checkSum = CheckSum.chkSum(data, offset, len);
            ipHeader.position(0);
            ipHeader.put(saved);
        }
        return internal && checkSum == 0 ? 0xFFFF : checkSum;
    }

    public int getSourcePort() {
        return udpHeader.get(0) >>> 16;
    }

    public int getDestPort() {
        return udpHeader.get(0) & 0xFFFF;
    }

    private boolean isValidPort(int port) {
        return port >= 1 && port <= 65535;
    }
}
