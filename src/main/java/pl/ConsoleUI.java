package pl;

import com.fazecast.jSerialComm.SerialPort;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public final class ConsoleUI implements UI {
    public int getRole() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Choose role:");
        System.out.println("1) Receiver");
        System.out.println("2) Sender");
        System.out.println("3) Exit program");
        System.out.print("Choice: ");

        String roleChoice;
        roleChoice = reader.readLine();

        while (!roleChoice.equals("1") && !roleChoice.equals("2") && !roleChoice.equals("3")) {
            System.out.println("Wrong input. Try again. ");
            System.out.print("Choice: ");
            roleChoice = reader.readLine();
        }

        return Integer.parseInt(roleChoice);
    }

    public boolean getIfUseCRC() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Use CRC?");
        System.out.println("1) Yes");
        System.out.println("2) No");
        System.out.print("Choice: ");

        String useCRCChoice;
        useCRCChoice = reader.readLine();

        while (!useCRCChoice.equals("1") && !useCRCChoice.equals("2")) {
            System.out.println("Wrong input. Try again. ");
            System.out.print("Choice: ");
            useCRCChoice = reader.readLine();
        }

        return useCRCChoice.equals("1");
    }

    public String getPortName() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        SerialPort[] serialPorts = SerialPort.getCommPorts();
        int index = 0;

        System.out.println("Choose port to use.");
        System.out.println("Available:");
        for (SerialPort port : serialPorts) {
            index++;
            System.out.println(index + ") " + port.getSystemPortName());
        }
        System.out.print("Choice: ");

        int portNameChoice = 0;
        do {
            try {
                portNameChoice = Integer.parseInt(reader.readLine());
                if (!(1 <= portNameChoice && portNameChoice <= index)) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                System.out.println("Wrong input. Try again.");
                System.out.print("Choice: ");
            }
        } while (!(1 <= portNameChoice && portNameChoice <= index));

        return serialPorts[portNameChoice - 1].getSystemPortName();
    }

    public void reportException(final Exception ex) {
        System.out.println("Error: " + ex.getMessage());
        System.out.println("Transmission cancelled\n");
    }
}
