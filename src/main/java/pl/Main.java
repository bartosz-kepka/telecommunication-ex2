package pl;

import java.io.IOException;


class Main {

    private static final int RECEIVER = 1;
    private static final int SENDER = 2;
    private static final int EXIT_PROGRAM = 3;

    public static void main(String[] args) throws IOException {

        UI ui = new ConsoleUI();
        Logger logger = new ConsoleLogger();

        while (true) {
            int role = ui.getRole();

            if (role == EXIT_PROGRAM) {
                break;
            } else {
                String portName = ui.getPortName();

                if (role == RECEIVER) {
                    boolean usingCRC = ui.getIfUseCRC();
                    Receiver receiver = new Receiver(portName, usingCRC, logger);

                    try {
                        receiver.receive();
                    } catch (Exception ex) {
                        ui.reportException(ex);
                    }
                    receiver.close();

                } else if (role == SENDER) {
                    Sender sender = new Sender(portName, logger);

                    try {
                        sender.send();
                    } catch (Exception ex) {
                        ui.reportException(ex);
                    }
                    sender.close();

                }
            }
        }
    }
}

