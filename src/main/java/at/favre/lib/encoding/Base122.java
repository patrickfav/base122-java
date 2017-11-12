package at.favre.lib.encoding;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class Base122 {
    private final static byte[] ILLEGAL_BYTES = new byte[]{
            0 // null
            , 10 // newline
            , 13 // carriage return
            , 34 // double quote
            , 38 // ampersand
            , 92 // backslash
    };

    static class Encoder {
        private static final byte STOP_BYTE = (byte) 0b1000_0000;
        private static final byte kShortened = 0b111; // Uses the illegal index to signify the last two-byte char encodes <= 7 bits.
        private int curIndex = 0;
        private int curBit = 0;
        private byte[] rawData;

        Encoder(byte[] rawData) {
            this.rawData = rawData;
        }

        byte next7Bit() {
            try {
                if (curIndex >= rawData.length) {
                    return STOP_BYTE;
                }

                // Shift, mask, unshift to get first part.
                byte firstByte = rawData[curIndex];
                int firstPart = ((0b11111110 >>> curBit) & firstByte) << curBit;
                // Align it to a seven bit chunk.
                firstPart >>>= 1;
                // Check if we need to go to the next byte for more bits.
                curBit += 7;
                if (curBit < 8) return (byte) firstPart; // Do not need next byte.
                curBit -= 8;
                curIndex++;
                // Now we want bits [0..curBit] of the next byte if it exists.
                if (curIndex >= rawData.length) return (byte) firstPart;
                byte secondByte = rawData[curIndex];
                int secondPart = ((0xFF00 >>> curBit) & secondByte) & 0xFF;
                // Align it.
                secondPart >>>= 8 - curBit;
                return (byte) (firstPart | secondPart);
            } finally {
                //System.out.println("curByte: " + curIndex + ", curBit: " + curBit);
            }
        }


        String encode() {
            byte sevenBits;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(rawData.length + (rawData.length / 8) + 1);
            while ((sevenBits = next7Bit()) != STOP_BYTE) {
                int illegalIndex;
                if ((illegalIndex = isIllegalCharacter(sevenBits)) != -1) {
                    // Since this will be a two-byte character, get the next chunk of seven bits.
                    byte nextSevenBits = next7Bit();

                    byte b1 = (byte) 0b11000010;
                    byte b2 = (byte) 0b10000000;
                    if (nextSevenBits == STOP_BYTE) {
                        b1 |= (0b111 & kShortened) << 2;
                        nextSevenBits = sevenBits; // Encode these bits after the shortened signifier.
                    } else {
                        b1 |= (0b111 & illegalIndex) << 2;
                    }

                    // Push first bit onto first byte, remaining 6 onto second.
                    byte firstBit = (byte) ((nextSevenBits & 0b01000000) > 0 ? 1 : 0);
                    b1 |= firstBit;
                    b2 |= nextSevenBits & 0b00111111;
                    outputStream.write(b1);
                    outputStream.write(b2);
                } else {
                    outputStream.write(sevenBits);
                }
            }
            return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
        }

        private int isIllegalCharacter(byte sevenBits) {
            for (int i = 0; i < ILLEGAL_BYTES.length; i++) {
                if (ILLEGAL_BYTES[i] == sevenBits) {
                    return i;
                }
            }
            return -1;
        }
    }

    public String encode(byte[] data) {
        return new Encoder(data).encode();
    }

}
