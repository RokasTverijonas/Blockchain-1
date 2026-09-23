public class MyHash {
    public static String hash(byte[] input) {
        byte[] hash = new byte[32];

        int seed = 293847597;

        for(int i = 0; i < hash.length; i++) {
            seed = seed * 28 + i;

            hash[i] = (byte) seed;
        }

        for(int i = 0; i < input.length; i++) {
            int index = i % 32;

            hash[index] ^= input[i];

            hash[index] *= 51;
        }

        hash = mixing(hash);

        String hex = "";

        for(byte i : hash) {
            hex += String.format("%02x", i & 0xff);
        }
        return hex;
    }

    private static byte[] mixing(byte[] hash) {
        for (int i = 0; i < hash.length; i++) {
            int next = (i + 1) % hash.length;
            int previous = (i - 1 + hash.length) % hash.length;

            hash[i] ^= hash[next];
            hash[i] ^= hash[previous];
            hash[i] *= 51;

        }
        return hash;
    }
}
