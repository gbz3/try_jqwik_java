package com.github.gbz3.try_jqwik_java;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public interface MipEncoder {

    default byte getFillByte() {
        return (byte) 0x40;
    }

    default int getBlockSize() {
        return 1014;
    }

    default byte[] getInsertBytes() {
        return new byte[]{getFillByte(), getFillByte()};
    }

    default byte[] encode(byte @NotNull [] aBytes) {
        final int blockDataSize = getBlockSize() - getInsertBytes().length;
        final byte[] result = new byte[(aBytes.length / blockDataSize + 1) * getBlockSize()];

        // 最終ブロックを埋める
        Arrays.fill(result, result.length - getBlockSize(), result.length - 1, getFillByte());

        // 入力データを転記する
        for (int srcPos = 0, destPos = 0; srcPos < aBytes.length;) {
            int copyLen = Math.min(blockDataSize, aBytes.length - srcPos);
            System.arraycopy(aBytes, srcPos, result, destPos, copyLen);
            srcPos += copyLen;
            destPos += copyLen;

            if (srcPos < aBytes.length) {
                System.arraycopy(getInsertBytes(), 0, result, destPos, getInsertBytes().length);
                destPos += getInsertBytes().length;
            }
        }
        return result;
    }

}
