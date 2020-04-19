package pl;

public final class ConsoleLogger implements Logger {
    public void log(final String message) {
        System.out.println(message);
    }
}
