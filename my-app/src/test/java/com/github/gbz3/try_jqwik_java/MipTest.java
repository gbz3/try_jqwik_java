package com.github.gbz3.try_jqwik_java;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Positive;
import net.jqwik.api.constraints.Size;
import net.jqwik.api.constraints.UseType;
import org.assertj.core.api.Assertions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MipTest {

    MipEncoder _enc = new MipEncoder() {};
    MipDecoder _dec = new MipDecoder() {};

    @Property
    void sameOriginal(@ForAll @Size(max = 8200) byte[] aData) {

        final byte[] encoded = _enc.encode(aData);
        Assertions.assertThat(encoded.length % _enc.getBlockSize()).isEqualTo(0);

        byte[] decoded = _dec.decode(encoded);
        byte[] actual = new byte[aData.length];
        System.arraycopy(decoded, 0, actual, 0, aData.length);
        Assertions.assertThat(actual).isEqualTo(aData);
    }

    @Disabled
    @Example
    public void testSameRecords() {
        List<VariableLengthField> fieldsOfRecord = List.of(
                VariableLengthField.of("A", "X", 1),
                VariableLengthField.of("B", "X", 2),
                VariableLengthField.of("C", "X", 3)
        );
        final byte[] expected = "ABBCCC".repeat(200).getBytes(StandardCharsets.UTF_8);
        final MipEncoder encoder = new MipEncoder() {};

        try (FileChannel stub = new FileChannelStub(encoder.encode(expected))) {
            AtomicInteger recordCount = new AtomicInteger(0);
            byte[] actual = StreamSupport.stream(MipSpliterator.of(stub, fieldsOfRecord), false)
                    .peek(m -> {
                        String values = m.values().stream()
                                .map(b -> "[" + HexFormat.of().withDelimiter(" ").withPrefix("0x").formatHex(b) + "]")
                                .collect(Collectors.joining(", "));
                        System.out.printf("#%03d: %s%n", recordCount.incrementAndGet(), values);
                    })
                    .flatMap(m -> m.values().stream())
                    .collect(ByteArrayOutputStream::new,
                            ByteArrayOutputStream::writeBytes,
                            (s1, s2) -> s1.writeBytes(s2.toByteArray()))
                    .toByteArray();

            Assertions.assertThat(Arrays.copyOf(actual, expected.length)).containsExactly(expected);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Example
    public void dummy() {
        List<VariableLengthField> fieldsOfRecord = List.of(
                VariableLengthField.of("a", "X", 3)
        );
        final int sizeOfRecord = fieldsOfRecord.stream().mapToInt(VariableLengthField::size).sum();
        final byte[] expected = "A".repeat(sizeOfRecord).repeat(1).getBytes(StandardCharsets.UTF_8);
        final MipEncoder encoder = new MipEncoder() {};

        try (FileChannel stub = new FileChannelStub(encoder.encode(expected))) {
//            AtomicInteger recordCount = new AtomicInteger(0);
            byte[] actual = StreamSupport.stream(MipSpliterator.of(stub, fieldsOfRecord), false)
//                    .peek(m -> {
//                        String values = m.values().stream()
//                                .map(b -> "[" + HexFormat.of().withDelimiter(" ").withPrefix("0x").formatHex(b) + "]")
//                                .collect(Collectors.joining(", "));
//                        System.out.printf("#%03d: %s %d%n", recordCount.incrementAndGet(), values, m.size());
//                    })
                    .flatMap(m -> m.values().stream())
                    .collect(ByteArrayOutputStream::new,
                            ByteArrayOutputStream::writeBytes,
                            (s1, s2) -> s1.writeBytes(s2.toByteArray()))
                    .toByteArray();

            Assertions.assertThat(Arrays.copyOf(actual, expected.length)).containsExactly(expected);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Disabled
    @Property
    public void sameRecords(@ForAll("validFields") List<VariableLengthField> fieldsOfRecord, @ForAll @IntRange(min = 1, max = 10000) int numOfRecord) {
        final int sizeOfRecord = fieldsOfRecord.stream().mapToInt(VariableLengthField::size).sum();
        final byte[] expected = "A".repeat(sizeOfRecord).repeat(numOfRecord).getBytes(StandardCharsets.UTF_8);
        final MipEncoder encoder = new MipEncoder() {};

        try (FileChannel stub = new FileChannelStub(encoder.encode(expected))) {
            //AtomicInteger recordCount = new AtomicInteger(0);
            byte[] actual = StreamSupport.stream(MipSpliterator.of(stub, fieldsOfRecord), false)
//                    .peek(m -> {
//                        String values = m.values().stream()
//                                .map(b -> "[" + HexFormat.of().withDelimiter(" ").withPrefix("0x").formatHex(b) + "]")
//                                .collect(Collectors.joining(", "));
//                        System.out.printf("#%03d: %s%n", recordCount.incrementAndGet(), values);
//                    })
                    .flatMap(m -> m.values().stream())
                    .collect(ByteArrayOutputStream::new,
                            ByteArrayOutputStream::writeBytes,
                            (s1, s2) -> s1.writeBytes(s2.toByteArray()))
                    .toByteArray();

            Assertions.assertThat(Arrays.copyOf(actual, expected.length)).containsExactly(expected);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Provide
    Arbitrary<List<VariableLengthField>> validFields() {
        Arbitrary<String> name = Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1);
        Arbitrary<String> types = Arbitraries.just("X");
        Arbitrary<Integer> size = Arbitraries.integers().between(1, 1013);

        Arbitrary<VariableLengthField> aField = Combinators.combine(name, types, size).as(VariableLengthField::of);
        return aField.list().ofMinSize(1).uniqueElements(VariableLengthField::name);
    }

}
