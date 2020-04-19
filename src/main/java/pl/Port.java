package pl;

import com.fazecast.jSerialComm.*;
import java.io.IOException;

import static com.fazecast.jSerialComm.SerialPort.TIMEOUT_READ_BLOCKING;
import static com.fazecast.jSerialComm.SerialPort.TIMEOUT_WRITE_BLOCKING;

public final class Port {
    SerialPort serialPort;

    public void initialize(final String portName) {
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setComPortParameters(9600, 8, 1, 0);
        serialPort.setComPortTimeouts( TIMEOUT_READ_BLOCKING | TIMEOUT_WRITE_BLOCKING, 20000, 10000);
        serialPort.openPort();
    }

    public void write(final byte[] buffer, int bytesToWrite, int offset) throws IOException {
        int bytesWritten = serialPort.writeBytes(buffer, bytesToWrite, offset);

        if (bytesWritten == -1) {
            throw new IOException("Unknown error occurred while writing to port " + serialPort.getSystemPortName());
        } else if (bytesToWrite != bytesWritten) {
            throw new IOException("Written " + bytesWritten + " bytes from given " + bytesToWrite + " bytes");
        }
    }

    public void read(final byte[] buffer, int bytesToRead, int offset) throws IOException {
        int bytesRead = serialPort.readBytes(buffer, bytesToRead,  offset);

        if (bytesRead == -1) {
            throw new IOException("Unknown error occurred while reading from port " + serialPort.getSystemPortName());
        } else if (bytesToRead != bytesRead) {
            throw new IOException("Read " + bytesRead + " bytes instead of supposed " + bytesToRead + " bytes");
        }
    }

    public String getName() {
        return serialPort.getSystemPortName();
    }

    public void flush() {
        int bytesToFlush = serialPort.bytesAvailable();
        serialPort.readBytes(new byte[bytesToFlush], bytesToFlush);
    }

    public void close() {
        serialPort.closePort();
    }
}
