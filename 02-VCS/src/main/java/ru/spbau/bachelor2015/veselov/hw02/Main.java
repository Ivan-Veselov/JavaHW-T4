package ru.spbau.bachelor2015.veselov.hw02;

import ru.spbau.bachelor2015.veselov.hw02.exceptions.DirectoryExpected;
import ru.spbau.bachelor2015.veselov.hw02.exceptions.InvalidDataInStorage;
import ru.spbau.bachelor2015.veselov.hw02.exceptions.VCSIsAlreadyInitialized;
import ru.spbau.bachelor2015.veselov.hw02.exceptions.VCSWasNotInitialized;

import java.io.IOException;
import java.nio.file.Paths;


/**
 * TODO
 */
public class Main {
    public static void main(String[] args)
            throws DirectoryExpected, IOException, VCSIsAlreadyInitialized, VCSWasNotInitialized, InvalidDataInStorage {
        if (args.length == 0) {
            System.out.println("Arguments expected");
            return;
        }

        if (args[0].equals("init")) {
            if (args.length != 2) {
                System.out.println("One argument expected");
                return;
            }

            Repository.initializeVCS(Paths.get(args[1]));
            return;
        }

        Repository repository = Repository.getRepository(Paths.get(""));
        switch (args[0]) {
            case "status":
                if (args.length != 1) {
                    System.out.println("No arguments expected");
                    return;
                }

                System.out.println(repository.getStatisticsProvider());
                break;

            default:
                System.out.println("Unknown command: " + args[0]);
        }
    }
}
