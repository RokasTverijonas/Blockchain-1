public class MyHash {
    public static int hash(byte[] input) {
        int hash = 8;

        for(byte i : input) {
            hash ^= i;
            hash *= 51;
        }
        
        return hash;
    }
}
