package ru.spbau.bachelor2015.veselov.md5;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException, IrregularFileException {
        if (args.length != 1) {
            System.out.println("One argument expected!");
            return;
        }

        Path path = Paths.get(args[0]);

        DirectoryHasher hasher = new DirectoryHasher();

        DirectoryHasher.MD5Hash hash1 = hasher.getHash(path);
        DirectoryHasher.MD5Hash hash2 = hasher.getHashConcurrently(path);

        if (!hash1.equals(hash2)) {
            System.out.println("Something went wrong, hashes are different!");
            return;
        }
    }
}
