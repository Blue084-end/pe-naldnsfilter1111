package ip;

import java.net.*;
import java.nio.*;

public class IPPacket {

    static short curID = (short) (Math.random() * Short.MAX_VALUE);
    static final Object ID_SYNC = new Object();

    protected IntBuffer ipHeader;
    protected int version;
    protected int len;
    protected int ipHdrlen;
    protected int offset;
    protected byte[] data;

    public IPPacket(byte[] packet, int offs, int len) {
        this.version = (packet[offs] >> 4) & 0x0F;
        this.data = packet;
        this.offset = offs;
        this.len = len;

        if (version == 4) {
            ipHdrlen = 20;
        } else if (version == 6) {
            ipHdrlen = 40;
        } else {
            throw new IllegalArgumentException("Invalid IP version: " + version);
        }

        this.ipHeader = ByteBuffer.wrap(packet, offs, ipHdrlen).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
    }

    public static IPPacket createInitialIPPacket(byte[] packet, int offs, int len, int version) {
        packet[offs] = (byte) ((version << 4) & 0xF0);
        IPPacket ip = new IPPacket(packet, offs, len);
        ip.initInitialIPHeader();
        return ip;
    }

    public static IPPacket createIPPacket(byte[] packet, int offs, int len, int version, int TTL, int prot, int[] sourceIP, int[] destIP) {
        packet[offs] = (byte) ((version << 4) & 0xF0);
        IPPacket ip = new IPPacket(packet, offs, len);
        ip.initIPHeader(TTL, prot, sourceIP, destIP);
        return ip;
    }

    protected void initInitialIPHeader() {
        if (version == 4) {
            int[] hdr = new int[5];
            hdr[0] = 0x45000000 + len;
            hdr[1] = generateId();
            hdr[2] = 0;
            hdr[3] = 0;
            hdr[4] = 0;
            ipHeader.position(0);
            ipHeader.put(hdr);
        } else if (version == 6) {
            int[] hdr = new int[2];
            hdr[0] = version << 28;
            hdr[1] = ((len - 40) << 16);
            ipHeader.position(0);
            ipHeader.put(hdr);
        }
    }

    protected void initIPHeader(int TTL, int prot, int[] sourceIP, int[] destIP) {
        if (version == 4) {
            int[] hdr = new int[5];
            hdr[0] = 0x45000000 + len;
            hdr[1] = generateId();
            hdr[2] = (TTL << 24) + (prot << 16);
            hdr[3] = sourceIP[0];
            hdr[4] = destIP[0];
            ipHeader.position(0);
            ipHeader.put(hdr);
            hdr[2] += calculateCheckSum();
            ipHeader.put(2, hdr[2]);
        } else if (version == 6) {
            int[] hdr = new int[2];
            hdr[0] = version << 28;
            hdr[1] = ((len - 40) << 16) + (prot << 8) + TTL;
            ipHeader.position(0);
            ipHeader.put(hdr);
            ipHeader.put(sourceIP);
            ipHeader.put(destIP);
        }
    }

    private static int generateId() {
        synchronized (ID_SYNC) {
            curID++;
            return ((int) curID) << 16;
        }
    }

    private int calculateCheckSum() {
        return version == 4 ? CheckSum.chkSum(data, offset, 20) : 0;
    }

    public int checkCheckSum() {
        return calculateCheckSum();
    }

    public int[] getSourceIP() {
        return version == 4 ? copyFromHeader(3, 1) : copyFromHeader(2, 4);
    }

    public int[] getDestIP() {
        return version == 4 ? copyFromHeader(4, 1) : copyFromHeader(6, 4);
    }

    private int[] copyFromHeader(int pos, int count) {
        ipHeader.position(pos);
        int[] result = new int[count];
        ipHeader.get(result, 0, count);
        return result;
    }

    public int getTTL() {
        return version == 4 ? ipHeader.get(2) >>> 24 : ipHeader.get(1) & 0xFF;
    }

    public int getProt() {
        return version == 4 ? (ipHeader.get(2) >>> 16) & 0xFF : (ipHeader.get(1) >>> 8) & 0xFF;
    }

    public int getLength() {
        return version == 4 ? ipHeader.get(0) & 0xFFFF : 40 + (ipHeader.get(1) >>> 16);
    }

    public byte[] getData() {
        return data;
    }

    public int getOffset() {
        return offset;
    }

    public int getHeaderLength() {
        return ipHdrlen;
    }
}
