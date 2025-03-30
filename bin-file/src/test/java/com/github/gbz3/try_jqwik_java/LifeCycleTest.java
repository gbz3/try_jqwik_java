package com.github.gbz3.try_jqwik_java;

import net.jqwik.api.*;
import net.jqwik.api.constraints.Size;
import net.jqwik.api.lifecycle.AfterTry;
import net.jqwik.api.lifecycle.BeforeTry;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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

    static void printBlocks(byte @NotNull [] bytes) {
        var orgBuff = ByteBuffer.wrap(bytes);
        var dataBuff = ByteBuffer.allocate(bytes.length / 1014 * 1012);
        while (orgBuff.hasRemaining()) {
            var blockBytes = new byte[1012];
            orgBuff.get(blockBytes);
            dataBuff.put(blockBytes);
            orgBuff.position(orgBuff.position() + 2);
        }

        dataBuff.flip();
        while (dataBuff.hasRemaining()) {
            var size = dataBuff.getInt();
            if (size == 0) break;
            System.out.printf("%3d ", size);
            dataBuff.position(dataBuff.position() + size);
        }
        var map = new HashMap<Byte, Integer>() {
            @Contract(pure = true)
            @Override
            public @NotNull String toString() {
                return "{" + super.keySet().stream().map(k -> String.format(" 0x%02X=>%d", k, super.get(k))).collect(Collectors.joining(",")) + " }";
            }
        };
        while (dataBuff.hasRemaining()) {
            var b = dataBuff.get();
            map.compute(b, (key, val) -> val == null ? 1 : val + 1);
        }
        System.out.println(map);
    }

    @Property
    void test(@ForAll("ebcdicBytesList") @Size(min = 1, max = 10) @NotNull List<byte[]> bytesList) throws IOException {
        var tempFile = tempDirectory.resolve("test.txt");

        // データ部分作成
        var dataBytes = ByteBuffer.allocate(bytesList.stream().mapToInt(e -> Integer.BYTES + e.length).sum());
        for (var bytes : bytesList) {
            dataBytes.putInt(bytes.length);
            dataBytes.put(bytes);
        }

        // ブロック化
        var blockDataSize = 1012;
        var dataSize = (dataBytes.capacity() / blockDataSize + 1) * (blockDataSize + 2);
        var blockedBytes = ByteBuffer.allocate(dataSize);
        dataBytes.flip();
        while (dataBytes.hasRemaining()) {
            var blockDataBytes = new byte[Math.min(dataBytes.limit() - dataBytes.position(), blockDataSize)];
            dataBytes.get(blockDataBytes);
            blockedBytes.put(blockDataBytes);
            if (blockDataBytes.length == blockDataSize) {
                blockedBytes.put((byte) 0x40);
                blockedBytes.put((byte) 0x40);
            }
        }
        blockedBytes.putInt(0);
        while (blockedBytes.hasRemaining()) {
            blockedBytes.put((byte) 0x40);
        }

        Files.write(tempFile, blockedBytes.array());
        printBlocks(blockedBytes.array());

        Assertions.assertThat(Files.readAllBytes(tempFile)).isEqualTo(blockedBytes.array());
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
                .array(byte[].class).ofMinSize(1).ofMaxSize(100);
    }

    @Provide
    Arbitrary<List<byte[]>> ebcdicBytesList() {
        return ebcdicBytes().list();
    }

}
