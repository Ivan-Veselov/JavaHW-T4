package ru.spbau.bachelor2015.veselov.hw04.client;

import ru.spbau.bachelor2015.veselov.hw04.client.exceptions.ConnectionWasClosedException;
import ru.spbau.bachelor2015.veselov.hw04.messages.FTPListAnswerMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * Entry point of a client program. This class allows to make requests to server.
 */
public class Main {
    /**
     * Entry point.
     *
     * @param args two arguments are expected. First is a string representation of a host, second is port. Together they
     *             form an address of a server to which client will be connected.
     * @throws IOException if any IO exception occurs during application work.
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Two arguments expected: <host> <port>");
            return;
        }

        try (Client client = new Client(args[0], Integer.parseInt(args[1]));
             InputStreamReader inputStreamReader = new InputStreamReader(System.in);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            boolean shouldRun = true;
            while (shouldRun) {
                String inputLine = bufferedReader.readLine();
                Scanner scanner = new Scanner(inputLine);

                if (!scanner.hasNext()) {
                    continue;
                }

                String command = scanner.next();
                switch (command) {
                    case "disconnect":
                        shouldRun = false;
                        break;

                    case "list":
                        if (!scanner.hasNext()) {
                            System.out.println("Argument expected: <path>");
                            break;
                        }

                        List<FTPListAnswerMessage.Entry> answer = client.list(scanner.next());
                        for (FTPListAnswerMessage.Entry entry : answer) {
                            System.out.println(entry);
                        }

                        break;

                    case "get":
                        if (!scanner.hasNext()) {
                            System.out.println("Two arguments expected: <path on server> <local path>");
                            break;
                        }

                        String source = scanner.next();

                        if (!scanner.hasNext()) {
                            System.out.println("Two arguments expected: <path on server> <local path>");
                            break;
                        }

                        String destination = scanner.next();

                        client.get(source, Paths.get(destination));

                        break;

                    default:
                        System.out.println("Unknown command: " + command);
                }
            }
        } catch (ConnectionWasClosedException e) {
            System.out.println("You were disconnected");
        }
    }
}
