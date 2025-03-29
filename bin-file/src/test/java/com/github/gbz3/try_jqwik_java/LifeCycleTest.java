package com.github.gbz3.try_jqwik_java;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.AfterTry;
import net.jqwik.api.lifecycle.BeforeTry;
import org.assertj.core.api.Assertions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class LifeCycleTest {

    private Path tempDirectory;

    @BeforeTry
    void setup() throws IOException {
        tempDirectory = Files.createTempDirectory("jqwik-test-");
    }

    @AfterTry
    void teardown() throws IOException {
        if (tempDirectory != null) {
            try (var walk = Files.walk(tempDirectory)) {
                walk.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                System.err.println("Failed to delete: " + path + " - " + e.getMessage());
                            }
                });
            }
        }
    }

    @Property
    void test(@ForAll("ebcdicBytes") byte[] bytes) throws IOException {
        var tempFile = tempDirectory.resolve("test.txt");
        Files.write(tempFile, bytes);

        Assertions.assertThat(Files.readAllBytes(tempFile)).isEqualTo(bytes);
    }

    static boolean isEbcdicNumber(byte b) {
        return (byte) 0xF0 <= b && b <= (byte) 0xF9;    // 0-9
    }

    static boolean isEbcdicLowerAlphabet(byte b) {
        return ((byte) 0x81 <= b && b <= (byte) 0x89)       // a-i
                || ((byte) 0x91 <= b && b <= (byte) 0x99)   // j-r
                || ((byte) 0xA2 <= b && b <= (byte) 0xA9)   // s-z
                ;
    }

    static boolean isEbcdicUpperAlphabet(byte b) {
        return ((byte) 0xC1 <= b && b <= (byte) 0xC9)   // A-I
                || ((byte) 0xD1 <= b && b <= (byte) 0xD9)   // J-R
                || ((byte) 0xE2 <= b && b <= (byte) 0xE9)   // S-Z
                ;
    }

    @Provide
    Arbitrary<byte[]> ebcdicBytes() {
        return Arbitraries.bytes()
                .filter(b -> isEbcdicNumber(b) || isEbcdicLowerAlphabet(b) || isEbcdicUpperAlphabet(b))
                .array(byte[].class).ofMinSize(1).ofMaxSize(10);
    }

}
