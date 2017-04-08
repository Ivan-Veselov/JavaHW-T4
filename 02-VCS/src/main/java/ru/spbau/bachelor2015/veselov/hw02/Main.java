package ru.spbau.bachelor2015.veselov.hw02;

import ru.spbau.bachelor2015.veselov.hw02.exceptions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * Main class which delegates user input to Repository class.
 */
public class Main {
    public static void main(String[] args)
            throws DirectoryExpected, IOException, InvalidDataInStorage,
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

            try {
                Repository.initializeVCS(Paths.get(args[1]));
            } catch (VCSIsAlreadyInitialized e) {
                System.out.println("VCS is already initialized");
            }

            return;
        }

        Repository repository;
        try {
            repository = Repository.getRepository(Paths.get(""));
        } catch (VCSWasNotInitialized e) {
            System.out.println("VCS was not initialized");
            return;
        }

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

            case "reset":
                if (args.length != 2) {
                    System.out.println("One argument expected");
                    break;
                }

                repository.resetFileState(Paths.get(args[1]));
                break;

            case "rm":
                if (args.length != 2) {
                    System.out.println("One argument expected");
                    break;
                }

                Path path = Paths.get(args[1]);
                Files.delete(path);
                repository.updateFileStateInIndex(path);
                break;

            case "clean":
                if (args.length != 1) {
                    System.out.println("No arguments expected");
                    break;
                }

                repository.removeUntrackedFiles();
                break;

            case "branch":
                switch (args.length) {
                    case 1:
                        System.out.println("Arguments expected");
                        break;

                    case 2:
                        try {
                            repository.createReference(args[1], repository.getCurrentCommit());
                        } catch (AlreadyExists e) {
                            System.out.println("Reference with such name already exists");
                        }

                        break;

                    case 3:
                        if (!args[1].equals("-d")) {
                            System.out.println("Unknown option: " + args[1]);
                            break;
                        }

                        try {
                            repository.deleteReference(args[2]);
                        } catch (ReferenceIsUsed e) {
                            System.out.println("Can't delete reference which is currently used");
                        } catch (NoSuchElement e) {
                            System.out.println("There is no reference with such name");
                        }

                        break;

                    default:
                        System.out.println("Too many arguments");
                }

                break;

            case "checkout":
                if (args.length != 2) {
                    System.out.println("One argument expected");
                    break;
                }

                try {
                    repository.restoreState(args[1]);
                } catch (IllegalArgumentException e) {
                    try {
                        repository.restoreState(repository.new Commit(new SHA1Hash(args[1])));
                    } catch (NoSuchElement ee) {
                        System.out.println("No such reference or commit");
                    }
                }
                break;

            case "merge":
                if (args.length != 2) {
                    System.out.println("One argument expected");
                    break;
                }

                try {
                    if (repository.mergeCommitWithCurrent(repository.getCommitByReference(args[1])) == null) {
                        System.out.println("Unable to merge");
                    }
                } catch (NoSuchElement e) {
                    System.out.println("There is no reference with such name");
                }

                break;

            default:
                System.out.println("Unknown command: " + args[0]);
        }
    }
}
