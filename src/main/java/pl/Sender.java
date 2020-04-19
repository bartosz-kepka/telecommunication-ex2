package pl;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import static pl.Codes.*;

public class Sender extends Transmitter{

    private InputStream inputStream;
    private Logger logger;

    public Sender(final String portName, final Logger logger) {
        super(portName);
        this.logger = logger;
    }

    public void send() throws Exception {
        logger.log("Sender: ACTIVE on port " + port.getName());

        byte[] sendBuffer = new byte[133];
        byte[] receiveBuffer = new byte[1];
        int blockNumber = 1, lastBlock;
        long fileLength;
        int bytesRead, bytesInPacket;

        // Open input file, count file length and calculate number of the last block of data
        File file = new File("to_send");
        fileLength = file.length();
        lastBlock = fileLength % 128 == 0 ? (int) (fileLength / 128) : (int) (fileLength / 128) + 1;

        // Open input stream from file
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            throw new FileNotFoundException("File " + file.getAbsolutePath() + " doesn't exist");
        }

        // Wait for the receiver to initialise transmission
        logger.log("Sender: waiting up to 1 minute for C or NAK");
        for (int attempt = 0; attempt < 6; attempt++) {
            try {
                port.read(receiveBuffer, 1, 0);

                if (receiveBuffer[0] == C || receiveBuffer[0] == NAK) {
                    port.flush();
                    break;
                }
            } catch (IOException ex) {
                if (attempt == 5) {
                    throw new TimeoutException("Receiver not initialising transmission. Didn't get C or NAK in 1 minute.");
                }
            }
        }

        // Decode requested type of checksum: 'C' for CRC, NAK for checksum modulo 255
        usingCRC = receiveBuffer[0] == C;

        // packet = header + block of data + checksum
        bytesInPacket = usingCRC ? 133 : 132;
        logger.log("Sender: got " + (usingCRC ? "C. Sending file with CRC" : "NAK. Sending file with checksum modulo 255"));

        // Start sending data
        boolean sendNextBlock = true;
        while (blockNumber <= lastBlock) {

            // Fill header - first 3 bytes
            sendBuffer[0] = SOH;
            sendBuffer[1] = (byte) (blockNumber % 255);
            sendBuffer[2] = (byte) (255 - blockNumber); // Block number complement to 255

            // If supposed to send next block of data, read next 128 bytes from file
            // If read less than 128 bytes, fill to 128 with SUB code
            if (sendNextBlock) {
                bytesRead = inputStream.read(sendBuffer, 3, 128);

                if (blockNumber == lastBlock) {
                    for (int i = bytesRead + 3; i < 131; i++) {
                        sendBuffer[i] = SUB;
                    }
                }
            }

            // Calculate checksum modulo 255 (1 byte) or CRC (2 bytes) and add to packet
            if (usingCRC) {
                int crc = Calculator.calculateCRC16(Arrays.copyOfRange(sendBuffer, 3, 131));
                sendBuffer[131] = (byte) (crc >> 8 & 0xFF);
                sendBuffer[132] = (byte) (crc & 0xFF);
            } else {
                sendBuffer[131] = Calculator.calculateChecksum(Arrays.copyOfRange(sendBuffer, 3, 131));
            }

            // Send whole packet: header + data + checksum/CRC
            // 132 bytes if using checksum modulo 255
            // 133 bytes if using CRC
//            port.write(sendBuffer, 3,0 );
//            port.write(sendBuffer, 128,3 );

//            if (usingCRC) {
//                port.write(sendBuffer, 2,131 );
//            } else {
//                port.write(sendBuffer, 1,131 );
//            }

            port.write(sendBuffer, bytesInPacket, 0);
            logger.log("Sender: send packet number " + blockNumber);

            // Read answer from receiver
            port.read(receiveBuffer, 1, 0);

            // If acknowledged, send next block, else send current block again
            if (receiveBuffer[0] == ACK) {
                sendNextBlock = true;
                blockNumber++;
            } else if (receiveBuffer[0] == NAK) {
                sendNextBlock = false;
            } else {
                throw new Exception("Got unexpected message. Expected ACK or NAK, got " + String.format("%02X", receiveBuffer[0]));
            }
        }

        // Send EOT code and close input file stream
        sendBuffer[0] = EOT;
        port.write(sendBuffer, 1, 0);
        inputStream.close();

        // Read answer from receiver
        port.read(receiveBuffer, 1, 0);

        // If acknowledged, transmission finished successfully
        if (receiveBuffer[0] == ACK) {
            logger.log("Sender: transmission succeeded! " + --blockNumber  + " blocks sent");
        }
        else {
            logger.log("Sender: end of transmission not acknowledged by the receiver");
        }
    }

    public void close() throws IOException {
        inputStream.close();
        super.close();
    }
}
