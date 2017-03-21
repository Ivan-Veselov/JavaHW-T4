package ru.spbau.bachelor2015.veselov.hw02;

import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class SHA1Hash implements Serializable {
    private final @NotNull String hexString;

    public SHA1Hash(final @NotNull byte[] data) {
        hexString = DigestUtils.sha1Hex(data);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof SHA1Hash)) {
            return false;
        }

        SHA1Hash other = (SHA1Hash) o;

        return hexString.equals(other.hexString);
    }

    public @NotNull String getHex() {
        return hexString;
    }
}
