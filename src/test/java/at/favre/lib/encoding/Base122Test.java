package at.favre.lib.encoding;

import org.apache.commons.codec.binary.BinaryCodec;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class Base122Test {
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void encode2Bytes() throws Exception {
        Base122.Encoder encoder = new Base122.Encoder(new byte[]{
                (byte) 0b0111111_0,
                (byte) 0b000001_01
        });

        assertExpected(0b0_0111111, encoder.next7Bit());
        assertExpected(0b0_0000001, encoder.next7Bit());
        assertExpected(0b0_0100000, encoder.next7Bit());
    }

    @Test
    public void encode3Bytes() throws Exception {
        Base122.Encoder encoder = new Base122.Encoder(new byte[]{
                (byte) 0b0111111_0,
                (byte) 0b000001_01,
                (byte) 0b00001_010
        });

        assertExpected(0b0_0111111, encoder.next7Bit());
        assertExpected(0b0_0000001, encoder.next7Bit());
        assertExpected(0b0_0100001, encoder.next7Bit());
        assertExpected(0b0_0100000, encoder.next7Bit());
    }

    @Test
    public void encode5Bytes() throws Exception {
        Base122.Encoder encoder = new Base122.Encoder(new byte[]{
                (byte) 0b0111101_0,
                (byte) 0b001001_01,
                (byte) 0b10001_010,
                (byte) 0b0010_0110,
                (byte) 0b011_01100
        });

        assertExpected(0b0_0111101, encoder.next7Bit());
        assertExpected(0b0_0001001, encoder.next7Bit());
        assertExpected(0b0_0110001, encoder.next7Bit());
        assertExpected(0b0_0100010, encoder.next7Bit());
        assertExpected(0b0_0110011, encoder.next7Bit());
        assertExpected(0b0_0110000, encoder.next7Bit());
    }

    private void assertExpected(int expected, byte byteWord) {
        String debugMsg = "\nExpected:\n\t" + String.valueOf(BinaryCodec.toAsciiChars(new byte[]{(byte) expected})) + "\nActual:\n\t" + String.valueOf(BinaryCodec.toAsciiChars(new byte[]{byteWord}));

        assertEquals(debugMsg, expected, byteWord);

        System.out.println(String.valueOf(BinaryCodec.toAsciiChars(new byte[]{byteWord})) + " " + new String(new byte[]{byteWord}, StandardCharsets.UTF_8));
    }
}