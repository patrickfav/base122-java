package at.favre.lib.encoding;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

public class EncoderTest {
    @Test
    public void encode() throws Exception {
        for (int i = 0; i < 32; i++) {
            System.out.println(new Base122().encode(Bytes.random(i + 4).array()));
        }
    }

}