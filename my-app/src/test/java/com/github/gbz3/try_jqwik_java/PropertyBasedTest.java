package com.github.gbz3.try_jqwik_java;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import org.assertj.core.api.*;

public class PropertyBasedTest {

    @Property
    boolean alwaysPositive(@ForAll int anInteger) {
        return Math.abs(anInteger) >= 0;
    }

    @Property
    void checkLength(@ForAll String s1, @ForAll String s2) {
        final var str = s1 + s2;
        Assertions.assertThat(str.length()).isGreaterThan(s1.length());
        Assertions.assertThat(str.length()).isGreaterThan(s2.length());
    }
}
