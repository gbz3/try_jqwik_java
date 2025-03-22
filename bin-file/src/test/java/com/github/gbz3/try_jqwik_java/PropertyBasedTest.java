package com.github.gbz3.try_jqwik_java;

import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Hashtable;

public class PropertyBasedTest {

    @Property
    boolean alwaysPositive(@ForAll @IntRange(min = Integer.MIN_VALUE + 1) int anInteger) {
        return Math.abs(anInteger) >= 0;
    }

    @Property
    boolean alwaysNegative(@ForAll @AnyText @StringLength(min = 1, max = 10) @NotNull String anString) {
        return !anString.contains("\t");
    }

    private final Charset charset = Charset.forName("IBM037");

    @Property
    boolean alwaysAlpha(@ForAll @AlphaChars @StringLength(min = 1, max = 10) @NotNull String anString) {
        var bytes = anString.getBytes(charset);
        for (var b : bytes) {
            if (
                    ((byte) 0x81 <= b && b <= (byte) 0x89) ||   // a-i
                    ((byte) 0x91 <= b && b <= (byte) 0x99) ||   // j-r
                    ((byte) 0xA2 <= b && b <= (byte) 0xA9) ||   // s-z
                    ((byte) 0xC1 <= b && b <= (byte) 0xC9) ||   // A-I
                    ((byte) 0xD1 <= b && b <= (byte) 0xD9) ||   // J-R
                    ((byte) 0xE2 <= b && b <= (byte) 0xE9)      // S-Z
            )
                continue;
            return false;
        }
        return true;
    }

    static void printLineBytes(byte @NotNull [] bytes) {
        System.out.print("[");
        for (var b : bytes) {
            if (b == (byte) 0x05) {
                System.out.print(" ] [");
            } else if (b != (byte) 0x15) {
                System.out.printf(" 0x%02x", b);
            }
        }
        System.out.println(" ]");
    }

    @Test
    void testTemporaryFile(@TempDir @NotNull Path tempDir) throws IOException {
        var tmpFile = tempDir.resolve("tmp.dat");
        Files.writeString(tmpFile, "0123\t456\n789\n\t\t\t\n", charset);

        var cnt = new Hashtable<Byte, Integer>();
        try (var fis = new FileInputStream(tmpFile.toFile());
             var fch = fis.getChannel()
        ) {
            var buff = ByteBuffer.allocate(4096);

            while (fch.read(buff) != -1) {
                buff.flip();
                buff.mark();    // 1行分のデータの先頭位置にマーク

                while (buff.hasRemaining()) {
                    var b = buff.get();
                    //System.out.printf("[%X] pos=%d\n", b, buff.position());
                    cnt.compute(b, (k, v) -> (v == null)? 1: v + 1);
                    if (b == (byte) 0x15) {
                        // 1行分のデータを取得
                        var endPos = buff.position();
                        buff.reset();   // pos をマークした位置に戻す
                        var lineBytes = new byte[endPos - buff.position()];
                        buff.get(lineBytes);
                        printLineBytes(lineBytes);

                        buff.mark();    // 1行分のデータの先頭位置にマーク
                    }
                }

                // バッファを圧縮
                buff.reset();
                buff.compact();
            }

            // 最後の改行より後ろのデータがあれば処理
            buff.flip();
            if (buff.hasRemaining()) {
                var otherBytes = new byte[buff.limit()];
                buff.get(otherBytes);
                printLineBytes(otherBytes);
            }
        }

        cnt.forEach((k, v) -> System.out.printf("  cnt[0x%02X]: % 3d\n", k, v));
    }

}
