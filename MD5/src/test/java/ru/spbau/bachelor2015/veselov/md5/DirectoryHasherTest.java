package ru.spbau.bachelor2015.veselov.md5;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DirectoryHasherTest {
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    private MessageDigest messageDigest = MessageDigest.getInstance("MD5");

    public DirectoryHasherTest() throws NoSuchAlgorithmException {
    }

    @Test
    public void hashOfSingleFile() throws Exception {
        File file = testFolder.newFile();
        Files.write(file.toPath(), new byte[] {1, 2, 3});

        messageDigest.update(Files.readAllBytes(file.toPath()));

        DirectoryHasher hasher = new DirectoryHasher();

        assertThat(hasher.getHash(file.toPath()).getBytes(), is(equalTo(messageDigest.digest())));
    }
}