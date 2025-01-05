package com.github.gbz3.try_jqwik_java;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record VariableLengthField(String name, String type, int size) {

    @Contract("_, _, _ -> new")
    public static @NotNull VariableLengthField of(String name, String type, int size) {
        if (name == null || name.trim().isEmpty() || !name.matches("[a-zA-Z]+")) throw new IllegalArgumentException();
        if (type == null || type.trim().isEmpty()) throw new IllegalArgumentException();
        if (size <= 0) throw new IllegalArgumentException();
        return new VariableLengthField(name, type, size);
    }

}
