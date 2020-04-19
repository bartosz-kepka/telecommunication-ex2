package pl;

import java.io.IOException;

public abstract class Transmitter {
    Port port = new Port();
    boolean usingCRC;

    public Transmitter(final String portName) {
        port.initialize(portName);
    }

    /**
     * Must be overridden and called in subclass.
     *
     * @throws IOException
     */
    void close() throws IOException {
        port.close();
    }
}
