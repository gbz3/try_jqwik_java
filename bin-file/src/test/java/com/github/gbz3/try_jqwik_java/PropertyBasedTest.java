package com.github.gbz3.try_jqwik_java;

import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.Arrays;

public class PropertyBasedTest {

    @Property
    boolean alwaysPositive(@ForAll @IntRange(min = Integer.MIN_VALUE + 1) int anInteger) {
        return Math.abs(anInteger) >= 0;
    }

    @Property
    boolean alwaysNegative(@ForAll @AnyText @StringLength(min = 1, max = 10) @NotNull String anString) {
        return !anString.contains("\t");
    }

    @Property
    boolean alwaysAlpha(@ForAll @AlphaChars @StringLength(min = 1, max = 10) @NotNull String anString) {
        var bytes = anString.getBytes(Charset.forName("IBM037"));
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

}
