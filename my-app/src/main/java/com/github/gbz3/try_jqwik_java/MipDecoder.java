package com.github.gbz3.try_jqwik_java;

import org.jetbrains.annotations.NotNull;

public interface MipDecoder {

    default byte getFillByte() {
        return (byte) 0x40;
    }

    default int getBlockSize() {
        return 1014;
    }

    default byte[] getInsertBytes() {
        return new byte[]{getFillByte(), getFillByte()};
    }

    default byte[] decode(byte @NotNull [] mipBytes) {
        if (mipBytes.length % getBlockSize() != 0) {
            throw new IllegalArgumentException("mipBytes must be a multiple of " + getBlockSize());
        }
        final int blockDataSize = getBlockSize() - getInsertBytes().length;
        final byte[] result = new byte[(mipBytes.length / getBlockSize()) * blockDataSize];

        // 入力データを転記する
        for (int srcPos = 0, destPos = 0; destPos < result.length;) {
            System.arraycopy(mipBytes, srcPos, result, destPos, blockDataSize);
            srcPos += blockDataSize + getInsertBytes().length;
            destPos += blockDataSize;
        }

        return result;
    }

}
