package com.github.gbz3.try_jqwik_java;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class MipSpliterator implements Spliterator<LinkedHashMap<String, byte[]>> {
    private final FileChannel input;
    private int inputPosition = 0;

    private final List<VariableLengthField> fieldsOfRecord;
    private final int sizeOfRecord;

    private MipSpliterator(FileChannel input, @NotNull List<VariableLengthField> fields) {
        this.input = input;
        this.fieldsOfRecord = fields;
        this.sizeOfRecord = fields.stream().mapToInt(VariableLengthField::size).sum();
    }

    @Contract("_, _ -> new")
    public static @NotNull MipSpliterator of(FileChannel input, @NotNull List<VariableLengthField> fields) {
        if (fields.isEmpty()) {
            throw new IllegalArgumentException("MipSpliterator requires at least one field");
        }
        return new MipSpliterator(input, fields);
    }

    @Override
    public boolean tryAdvance(Consumer<? super LinkedHashMap<String, byte[]>> action) {
        final int mipDataLen = 1012;
        final int mipFillLen = 2;
        final int mipBlockLen = mipDataLen + mipFillLen;
        try {
            System.out.println("### " + (inputPosition + sizeOfRecord + sizeOfRecord / mipDataLen * mipFillLen) + " : " + input.size());
            if (inputPosition + sizeOfRecord + sizeOfRecord / mipDataLen * mipFillLen > input.size()) {
                return false;
            }

            // レコードを切り出す
            LinkedHashMap<String, byte[]> record = new LinkedHashMap<>();
            for (VariableLengthField field : fieldsOfRecord) {
                // 1フィールド分のバッファを入力データで埋める
                ByteBuffer fieldBuffer = ByteBuffer.allocate(field.size());
                while (fieldBuffer.position() < fieldBuffer.capacity()) {
                    // 1フィールド分の入力データが Filler で分割されている場合、分割して読み取る
                    final boolean isDivide = inputPosition % mipBlockLen + fieldBuffer.capacity() - fieldBuffer.position() > mipDataLen;
                    System.out.println(">>> " + (isDivide ? mipDataLen - inputPosition % mipBlockLen : field.size()));
                    System.out.println("+++ " + fieldBuffer.position() + " : " + fieldBuffer.capacity());
                    fieldBuffer.limit(isDivide ? mipDataLen - inputPosition % mipBlockLen : field.size());
                    inputPosition += input.read(fieldBuffer, inputPosition);
                    inputPosition += inputPosition % mipBlockLen == mipDataLen ? mipFillLen : 0;
                }

                fieldBuffer.flip();
                record.put(field.name(), fieldBuffer.array());
            }
            action.accept(record);

            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Spliterator<LinkedHashMap<String, byte[]>> trySplit() {
        // 並列処理は実装しない
        return null;
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return ORDERED | NONNULL;
    }

}
