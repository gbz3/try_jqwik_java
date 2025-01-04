package com.github.gbz3.try_jqwik_java;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.Size;
import org.assertj.core.api.Assertions;

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

}
