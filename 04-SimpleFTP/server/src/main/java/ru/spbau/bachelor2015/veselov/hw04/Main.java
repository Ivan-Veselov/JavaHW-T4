package ru.spbau.bachelor2015.veselov.hw04;

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
        Server server = new Server(Paths.get(""), 10000);

        boolean shouldRun = true;

        while (shouldRun) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(System.in);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
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
                        System.out.println("Unknown command");
                }
            }
        }
    }
}
