package ru.spbau.bachelor2015.veselov.hw02;

import ru.spbau.bachelor2015.veselov.hw02.exceptions.DirectoryExpected;
import ru.spbau.bachelor2015.veselov.hw02.exceptions.VCSIsAlreadyInitialized;

import java.io.IOException;
import java.nio.file.Paths;


/**
 * TODO
 */
public class Main {
    public static void main(String[] args) throws DirectoryExpected, IOException, VCSIsAlreadyInitialized {
        if (args.length == 0) {
            System.out.println("Arguments expected");
            return;
        }

        switch (args[0]) {
            case "init":
                if (args.length != 2) {
                    System.out.println("One argument expected");
                    return;
                }

                Repository.initializeVCS(Paths.get(args[1]));
                break;

            default:
                System.out.println("Unknown command: " + args[0]);
        }
    }
}
