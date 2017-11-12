package at.favre.lib.encoding;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class DecoderTest {
    @Test
    public void decode() throws Exception {
        for (int i = 0; i < 32; i++) {
            byte[] rnd = Bytes.random(i + 4).array();
            String encoded = new Base122().encode(rnd);
            byte[] decoded = new Base122().decode(encoded);
            System.out.println(encoded);
            assertArrayEquals(rnd, decoded);
        }
    }

}