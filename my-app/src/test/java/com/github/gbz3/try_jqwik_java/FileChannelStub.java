package com.github.gbz3.try_jqwik_java;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class FileChannelStub extends FileChannel {
    private final ByteBuffer content;
    private long position = 0;

    public FileChannelStub(byte @NotNull [] content) {
        this.content = ByteBuffer.wrap(content);
    }

    @Override
    public long size() throws IOException {
        return content.capacity();
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        return read(dst, this.position);
    }

    @Override
    public int read(ByteBuffer dst, long l) throws IOException {
        if (l >= content.capacity()) {
            return -1;
        }

        content.position((int) l);
        byte[] temp = new byte[Math.min(dst.remaining(), content.remaining())];
        content.get(temp);
        dst.put(temp);
        position = l + temp.length;
        return temp.length;
    }

    @Override
    protected void implCloseChannel() throws IOException {}

    @Override
    public long read(ByteBuffer[] byteBuffers, int i, int i1) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int write(ByteBuffer byteBuffer) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long write(ByteBuffer[] byteBuffers, int i, int i1) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long position() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileChannel position(long l) throws IOException {
        return null;
    }

    @Override
    public FileChannel truncate(long l) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void force(boolean b) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long transferTo(long l, long l1, WritableByteChannel writableByteChannel) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long transferFrom(ReadableByteChannel readableByteChannel, long l, long l1) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int write(ByteBuffer byteBuffer, long l) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public MappedByteBuffer map(MapMode mapMode, long l, long l1) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileLock lock(long l, long l1, boolean b) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileLock tryLock(long l, long l1, boolean b) throws IOException {
        throw new UnsupportedOperationException();
    }

}
