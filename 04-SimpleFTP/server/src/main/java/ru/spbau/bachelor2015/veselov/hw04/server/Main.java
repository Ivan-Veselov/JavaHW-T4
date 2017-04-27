package ru.spbau.bachelor2015.veselov.hw04.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

/**
 * Entry point of a server application. This class allows to start and stop server through a console.
 */
public class Main {
    /**
     * Entry method.
     *
     * @param args two arguments are expected. First is a path to a folder which will be tracked by server, second is a
     *             port which the server will be bound to.
     * @throws IOException any IO exception which may occur during reading of commands.
     * @throws InterruptedException if main thread was interrupted.
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
