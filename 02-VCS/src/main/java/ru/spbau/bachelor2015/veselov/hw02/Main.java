package ru.spbau.bachelor2015.veselov.hw02;

import ru.spbau.bachelor2015.veselov.hw02.exceptions.*;

import java.io.IOException;
import java.nio.file.Paths;


/**
 * TODO: impl, javadocs, exceptions
 */
public class Main {
    public static void main(String[] args)
            throws DirectoryExpected, IOException, VCSIsAlreadyInitialized, VCSWasNotInitialized, InvalidDataInStorage,
            FileFromWorkingDirectoryExpected, RegularFileExpected {
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
                    break;
                }

                System.out.println(repository.getStatisticsProvider());
                break;

            case "add":
                if (args.length != 2) {
                    System.out.println("One argument expected");
                    break;
                }

                repository.updateFileStateInIndex(Paths.get(args[1]));
                break;

            case "commit":
                if (args.length != 2) {
                    System.out.println("One argument expected");
                    break;
                }

                repository.newCommitFromIndex(args[1]);
                break;

            case "log":
                if (args.length != 1) {
                    System.out.println("No arguments expected");
                    break;
                }

                for (Repository.Commit commit : repository.getHistoryForCurrentCommit()) {
                    System.out.println(commit + "\n");
                }

                break;

            default:
                System.out.println("Unknown command: " + args[0]);
        }
    }
}
