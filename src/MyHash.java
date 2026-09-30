public class MyHash {
    // initial state constants (8 words = 256 bits)
    private static final int[] INIT = {
            0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a,
            0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19
    };
    // odd multiplier
    private static final int MUL = 0x9E3779B1;
    // number of final mixing rounds
    private static final int ROUNDS = 16;

    public static String hash(byte[] input) {
        int[] s = INIT.clone();

        // absorb input
        for (int i = 0; i < input.length; i++) {
            int idx = i & 7;
            s[idx] ^= (input[i] & 0xff);
            s[idx] = Integer.rotateLeft(s[idx] * MUL, 13);
            s[(idx + 1) & 7] += s[idx];
        }

        // add input length
        s[0] ^= input.length;
        s[1] += input.length * MUL;

        // final mixing
        for (int r = 0; r < ROUNDS; r++) {
            for (int i = 0; i < 8; i++) {
                int a = s[i];
                int b = s[(i + 1) & 7];
                int c = s[(i + 5) & 7];
                a += Integer.rotateLeft(b, 7) ^ c;
                a *= MUL;
                a ^= a >>> 15;
                s[i] = a;
            }
        }

        // convert to hex
        StringBuilder hex = new StringBuilder();
        for (int w : s) hex.append(String.format("%08x", w));
        return hex.toString();
    }
}