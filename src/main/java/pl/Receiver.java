package pl;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import static pl.Codes.*;

public class Receiver extends Transmitter {

    private OutputStream outputStream;
    private Logger logger;

    public Receiver(final String portName, final boolean usingCRC, final Logger logger) {
        super(portName);
        this.usingCRC = usingCRC;
        this.logger = logger;
    }

    public void receive() throws Exception {
        logger.log("Receiver: ACTIVE using " + (usingCRC ? "CRC" : "checksum") + " on port " + port.getName());

        byte[] sendBuffer = new byte[1];
        byte[] receiveBuffer = new byte[133];
        int expectedBlockNumber, bytesInPacket;
        boolean endOfTransmission = false;

        // Open output file
        File file = new File("received");

        // Open output stream to file, overwrite file
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException ex) {
            throw new FileNotFoundException("File " + file.getAbsolutePath() + " doesn't exist");
        }

        // Encode requested type of checksum: 'C' for CRC, NAK for checksum modulo 255
        sendBuffer[0] = usingCRC ? C : NAK;

        // packet = header + block of data + checksum
        bytesInPacket = usingCRC ? 133 : 132;

        // Initialise transmission sending C or NAK and wait for first packet
        for (int attempt = 0; attempt < 6; attempt++) {
            try {
                port.flush();
                port.write(sendBuffer, 1, 0);
               logger.log("Receiver: sent " + (usingCRC ? "C" : "NAK"));
                port.read(receiveBuffer, bytesInPacket, 0);
                break;
            } catch (IOException ex) {
                if (attempt == 5) {
                    throw new TimeoutException("Sender not responding. Didn't get any answer in 1 minute.");
                }
                System.out.println(ex.getMessage());
            }
        }

        expectedBlockNumber = receiveBuffer[1];

        while (!endOfTransmission) {
            if (expectedBlockNumber == Byte.toUnsignedInt(receiveBuffer[1])
                    && Byte.toUnsignedInt(receiveBuffer[1]) + Byte.toUnsignedInt(receiveBuffer[2]) == 255) {

               logger.log("Receiver: got packet number " + receiveBuffer[1]);

                boolean correctCheck;
                if (usingCRC) {
                    int expectedCrc = Calculator.calculateCRC16(Arrays.copyOfRange(receiveBuffer, 3, 131));
                    int crc = Byte.toUnsignedInt(receiveBuffer[131]) << 8;
                    crc += Byte.toUnsignedInt(receiveBuffer[132]);


                    correctCheck = expectedCrc == crc;
                } else {
                    byte expectedChecksum = Calculator.calculateChecksum(Arrays.copyOfRange(receiveBuffer, 3, 131));
                    byte checksum = receiveBuffer[131];

                    correctCheck = expectedChecksum == checksum;
                }

                if (correctCheck) {
                   logger.log("Receiver: correct checksum, sending ACK");
                    sendBuffer[0] = ACK;
                } else {
                   logger.log("Receiver: wrong checksum, sending NAK");
                    sendBuffer[0] = NAK;
                }

                port.write(sendBuffer, 1, 0);

                port.read(receiveBuffer, 1, 0);

                if (receiveBuffer[0] == EOT) {
                    int bytesFilledWithSUB = 0;
                    int position = 130;

                    while (receiveBuffer[position--] == SUB) {
                        bytesFilledWithSUB++;
                    }

                    outputStream.write(receiveBuffer, 3, 128 - bytesFilledWithSUB);

                    endOfTransmission = true;
                    sendBuffer[0] = ACK;
                    port.write(sendBuffer, 1, 0);
                } else {

                    if (correctCheck) {
                        outputStream.write(receiveBuffer, 3, 128);
                        expectedBlockNumber++;
                    }
                    port.read(receiveBuffer, bytesInPacket - 1, 1);
                }

            } else {
                throw new Exception("Receiver: got wrong packet. Expected: " + expectedBlockNumber + " , got: " + receiveBuffer[2]);
            }

        }
       logger.log("Receiver: transmission succeeded! Blocks received: " + expectedBlockNumber);

    }

    public void close() throws IOException {
        outputStream.close();
        super.close();
    }
}
