package at.favre.encoding;

import org.junit.Before;
import org.junit.Test;

public class Base122Test {
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void encode() throws Exception {
        Base122.Encoder encoder = new Base122.Encoder(new byte[]{(byte) 0b01111110, (byte) 0b00000101});

        byte b1 = encoder.get7Bit();
        byte b2 = encoder.get7Bit();
        byte b3 = encoder.get7Bit();

        printBinary(b1);
        printBinary(b2);
        printBinary(b3);
    }

    private void printBinary(byte b1) {
        //System.out.println(new Base2Encoder().encode(new byte[]{b1}) + " " + new String(new byte[]{b1}, StandardCharsets.UTF_8));
    }
}