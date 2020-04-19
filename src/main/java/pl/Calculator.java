package pl;

public class Calculator {

    /**
     * Calculates CRC-16/XMODEM checksum.
     *
     * @param bytes data to calculate checksum from
     * @return checksum as int
     */
    public static int calculateCRC16(byte[] bytes) {
        int crc = 0x0000;

        for (int b : bytes) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;

                if (c15 ^ bit) {
                    crc ^= 0x1021;
                }
            }
        }

        return crc & 0xFFFF;
    }

    public static byte calculateChecksum(byte[] bytes) {
        int checksum = 0;

        for (byte b : bytes) {
            checksum += b;
        }
        checksum %= 256;

        return (byte) checksum;
    }

}
