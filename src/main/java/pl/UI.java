package pl;

import java.io.IOException;

public interface UI {
    int getRole() throws IOException;

    boolean getIfUseCRC() throws IOException;

    String getPortName() throws IOException;

    void reportException(Exception ex);
}
