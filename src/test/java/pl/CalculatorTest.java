package pl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Expected values: https://crccalc.com/
 */
class CalculatorTest {

    @Test
    public void calculateCRC16_Text1(){
        byte[] testData = "test".getBytes();
        int crc = Calculator.calculateCRC16(testData);

        assertEquals(39686, crc);
    }

    @Test
    public void calculateCRC16_Text2(){
        byte[] testData = "Xmodem  - implementacja protokołu transferu plikow".getBytes();
        int crc = Calculator.calculateCRC16(testData);

        assertEquals(56419, crc);
    }

    @Test
    public void calculateCRC16_Empty(){
        byte[] testData = "".getBytes();
        int crc = Calculator.calculateCRC16(testData);

        assertEquals(0, crc);
    }

    @Test
    void calculateChecksum_Text2() {
        byte[] testData = "Xmodem  - implementacja protokolu transferu plikow".getBytes();
        int checksum = Calculator.calculateChecksum(testData);

        assertEquals(16, checksum);
    }

    @Test
    void calculateChecksum_Empty() {
        byte[] testData = "".getBytes();
        int checksum = Calculator.calculateChecksum(testData);

        assertEquals(0, checksum);
    }
}