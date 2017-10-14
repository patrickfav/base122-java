package at.favre.lib.encoding;

public class Base122 {

    static class Encoder {
        private int curIndex = 0;
        private int curBit = 0;
        private byte[] rawData;

        Encoder(byte[] rawData) {
            this.rawData = rawData;
        }

        String encode() {
            return null;
        }

        byte next7Bit() {
            try {
                if (curIndex >= rawData.length)
                    throw new IllegalArgumentException("current index is greater than data length");

                // Shift, mask, unshift to get first part.
                byte firstByte = rawData[curIndex];
                byte firstPart = (byte) (((0b11111110 >>> curBit) & firstByte) << curBit);
                // Align it to a seven bit chunk.
                firstPart >>= 1;
                // Check if we need to go to the next byte for more bits.
                curBit += 7;
                if (curBit < 8) return firstPart; // Do not need next byte.
                curBit -= 8;
                curIndex++;
                // Now we want bits [0..curBit] of the next byte if it exists.
                if (curIndex >= rawData.length) return firstPart;
                byte secondByte = rawData[curIndex];
                byte secondPart = (byte) (((0xFF00 >>> curBit) & secondByte) & 0xFF);
                // Align it.
                secondPart >>= 8 - curBit;
                return (byte) (firstPart | secondPart);
            } finally {
                System.out.println("curByte: " + curIndex + ", curBit: " + curBit);
            }
        }
    }


    public String encode(byte[] data) {
        return new Encoder(data).encode();
    }

}
