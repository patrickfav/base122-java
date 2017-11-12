package at.favre.lib.encoding;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class Base122 {
    private static final byte kShortened = 0b111; // Uses the illegal index to signify the last two-byte char encodes <= 7 bits.
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

    public byte[] decode(String encodedBase122) {
        return new Decoder().decode(encodedBase122);
    }

    final static class Decoder {
        private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        private byte curByte = 0;
        private byte bitOfByte = 0;

        void pushNext7(byte nextElement) {
            nextElement <<= 1;
            // Align this byte to offset for current byte.
            curByte |= (nextElement >>> bitOfByte);
            bitOfByte += 7;
            if (bitOfByte >= 8) {
                outputStream.write(curByte);
                bitOfByte -= 8;
                // Now, take the remainder, left shift by what has been taken.
                curByte = (byte) ((nextElement << (7 - bitOfByte)) & 255);
            }
        }

        byte[] decode(String base122Data) {
            byte[] utf8Bytes = base122Data.getBytes(StandardCharsets.UTF_8);

            for (int i = 0; i < utf8Bytes.length; i++) {
                // Check if this is a two-byte character.
                if (utf8Bytes[i] > 127) {
                    // Note, the charCodeAt will give the codePoint, thus
                    // 0b110xxxxx 0b10yyyyyy will give => xxxxxyyyyyy
                    int illegalIndex = (utf8Bytes[i] >>> 8) & 7; // 7 = 0b111.
                    // We have to first check if this is a shortened two-byte character, i.e. if it only
                    // encodes <= 7 bits.
                    if (illegalIndex != kShortened) pushNext7(ILLEGAL_BYTES[illegalIndex]);
                    // Always push the rest.
                    pushNext7((byte) (utf8Bytes[i] & 127));
                } else {
                    // One byte characters can be pushed directly.
                    pushNext7(utf8Bytes[i]);
                }
            }
            return outputStream.toByteArray();
        }
    }

}
