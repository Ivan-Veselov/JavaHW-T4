package ru.spbau.bachelor2015.veselov.hw04;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        Server server = new Server(Paths.get(""));

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
