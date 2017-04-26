package ru.spbau.bachelor2015.veselov.hw04.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

/**
 * Entry point of a program. This class allows to start and stop server through a console.
 */
public class Main {
    /**
     * Entry method.
     *
     * @param args arguments are ignored.
     * @throws IOException any IO exception which may occur during reading of commands.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 2) {
            System.out.println("Two arguments expected: <tracked folder> <port>");
            return;
        }

        Server server = new Server(Paths.get(args[0]), Integer.parseInt(args[1]));

        boolean shouldRun = true;

        try (InputStreamReader inputStreamReader = new InputStreamReader(System.in);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            while (shouldRun) {
                String command = bufferedReader.readLine();
                switch (command) {
                    case "start":
                        server.start();
                        break;

                    case "stop":
                        server.stop();
                        break;

                    case "exit":
                        shouldRun = false;
                        break;

                    default:
                        System.out.println("Unknown command: " + command);
                }
            }
        }
    }
}
