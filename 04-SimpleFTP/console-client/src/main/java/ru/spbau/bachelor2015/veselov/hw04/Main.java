package ru.spbau.bachelor2015.veselov.hw04;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

/**
 * Entry point of a client program. This class allows to make requests to server.
 */
public class Main {
    /**
     * Entry method.
     *
     * @param args first argument is a hostname, second argument is a port.
     * @throws IOException any IO exception which may occur during reading of commands.
     */
    public static void main(String[] args) throws IOException {
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
                            System.out.println("Argument expected");
                            break;
                        }

                        FTPListAnswerMessage answer = client.list(scanner.next());
                        for (FTPListAnswerMessage.Entry entry : answer.getContent()) {
                            System.out.println(entry);
                        }

                        break;

                    default:
                        System.out.println("Unknown command: " + command);
                }
            }
        }
    }
}
