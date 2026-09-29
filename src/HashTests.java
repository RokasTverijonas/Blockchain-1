import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class HashTests {
    //ascii simboliain skirti teksto generavimui
    private final static String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final static int seed = 283;

    public static void main(String[] args) throws Exception {
        formatTest();
        determinismTest();
        efficiencyTest();
        collisionTest();;
        structuredCollisionTest();
        avalancheTest();
        SaltTest();
    }

    //tikrinamas reikalingas formatas
    static void formatTest() throws Exception {
        System.out.println("2 Ekperimentas");

        File folder = new File("../test_files");
        File[] files = folder.listFiles();

        if (files == null) {
            System.out.println("Katalogas nerastas arba tučias: " + folder.getAbsolutePath());
            return;
        }

        for (File file : files) {
            byte[] data = Files.readAllBytes(file.toPath());
            String hash = MyHash.hash(data);

            boolean length = hash.length() == 64;
            boolean format = hash.matches("[0-9a-f]+");
            String status = (length && format) ? "Pavyko" : "Klaida";

            System.out.println(status + " | " + file.getName() + " | baitai: " + data.length + " | hash ilgis: " + hash.length() + " | hash: " + hash);
        }
    }

    static void determinismTest() throws Exception {
        File folder = new File("../test_files");
        File[] files = folder.listFiles();

        if (files == null) {
            System.out.println("Katalogas nerastas arba tučias: " + folder.getAbsolutePath());
                return;
        }

        //kartotiniai kvietimai
        System.out.println("KARTOTINIŲ KVIETIMŲ TESTAS:");
        for (File file : files) {
            byte[] input = Files.readAllBytes(file.toPath());

            String h1 = MyHash.hash(input);
            String h2 = MyHash.hash(input);
            String h3 = MyHash.hash(input);

            boolean equal = h1.equals(h2) && h2.equals(h3);

            System.out.println((equal ? "Pavyko" : "Klaida") + " | " + file.getName() + " | " + h1);
        }

        //A, B, A seka
        System.out.println("sekos (A, B, A) TESTAS:");
        byte[] A = Files.readAllBytes(new File("../test_files/ascii1.txt").toPath());
        byte[] B = Files.readAllBytes(new File("../test_files/ascii2.txt").toPath());

        String a = MyHash.hash(A);
        String b = MyHash.hash(B);
        String a1 = MyHash.hash(A);

        System.out.println(a.equals(a1) ? "A sutampa, pavyko" : "A skiriasi, nepavyko");
        System.out.println("A1: " + a);
        System.out.println("B: " + b);
        System.out.println("A2: " + a1);


    }

    static void efficiencyTest() throws Exception{
        byte[] all = Files.readAllBytes(new File("../konstitucija.txt").toPath());
        //kiekvienos eilutes pabaigos pozicija
        List<Integer> lineEnds = new ArrayList<>();
        for (int i = 0; i < all.length; i++) {
            if (all[i] == '\n') {
                lineEnds.add(i + 1);
            }
        }
        //jei failas baigsis be '\n', paskutine eilute vistiek itraukiama
        if (all.length > 0 && all[all.length - 1] != '\n') {
            lineEnds.add(all.length);
        }

        int total = lineEnds.size();
        System.out.println("Iš viso eilučių: " + total + ", baitų: " + all.length);

        //saugomi eiluciu skaiciai
        List<Integer> sizes = new ArrayList<>();
        for (int i = 1; i < total; i *= 2) {
            sizes.add(i);
        }
        sizes.add(total);

        //sudaromos teksto istraukos
        List<byte[]> excerpts = new ArrayList<>();
        for (int n : sizes) {
            excerpts.add(Arrays.copyOfRange(all, 0, lineEnds.get(n - 1)));
        }

        int warmup = 10;
        int calls = 1000;
        int repeats = 5;
        //rezultatu issaugojimui
        long resultSum = 0;

        System.out.println("Eilutes;Baitai;Vidurkis_ns;Min_ns;Max_ns");
        for(int k = 0; k < sizes.size(); k++) {
            byte[] data = excerpts.get(k);

            //apsilimas
            for (int i = 0; i < warmup; i++) {
                MyHash.hash(data);
            }

            double sum = 0;
            double min = Double.MAX_VALUE;
            double max = 0;

            //matavimai
            for (int r = 0; r < repeats; r++) {
                double start = System.nanoTime();
                for (int i = 0; i < calls; i++) {
                    resultSum += MyHash.hash(data).charAt(0);
                }
                double perHash = (System.nanoTime() - start) / calls;

                sum += perHash;
                min = Math.min(min, perHash);
                max = Math.max(max, perHash);
            }

            System.out.printf("%d;%d;%.1f;%.1f;%.1f;%n",
                    sizes.get(k), data.length, sum / repeats, min, max);
        }

        System.err.println("resultSum=" + resultSum);
    }

    static void collisionTest() {
        int[] lengths = {10, 100, 500, 1000};
        int pairs = 100000;
        Random rand = new Random(seed);

        System.out.println("Ilgis;PorosKolizijos;GrupesSuKolizija");

        for (int len : lengths) {
            int pairCollisions = 0;
            int groupCollisions = 0;

            Set<String> seenInputs = new HashSet<>();
            Map<String, String> hashAndInput = new HashMap<>();



            for (int p = 0; p < pairs; p++) {
                String a = randomAscii(rand, len);
                String b = randomAscii(rand, len);
                //uztikriname, kad poros elementai nera tokie patys
                while (b.equals(a)) {
                    b = randomAscii(rand, len);
                }

                String  hashA = MyHash.hash(a.getBytes(StandardCharsets.US_ASCII));
                String  hashB = MyHash.hash(b.getBytes(StandardCharsets.US_ASCII));

                if (hashA.equals(hashB)) {
                    pairCollisions++;
                }

                if (!seenInputs.contains(a)) {
                    seenInputs.add(a);
                    if (hashAndInput.containsKey(hashA)) {
                        System.err.println("Kolizija | " + hashAndInput.get(hashA) + " | " + a);
                        groupCollisions++;
                    } else {
                        hashAndInput.put(hashA, a);
                    }
                }

                if (!seenInputs.contains(b)) {
                    seenInputs.add(b);
                    if (hashAndInput.containsKey(hashB)) {
                        System.err.println("Kolizija | " + hashAndInput.get(hashB) + " | " + b);
                        groupCollisions++;
                    } else {
                        hashAndInput.put(hashB, b);
                    }
                }

            }

            System.out.println(len + ";" + pairCollisions + ";" + groupCollisions);
        }


    }

    static void structuredCollisionTest() {
        String[] inputs = {"abc", "cba", "bca", "bac", "cab",
        "bbbbbbbbbbbbbbb", "aaaaaaaa", "cccccccc", "bababa"};

        Map<String, String> seen = new HashMap<>();
        int collisions = 0;

        System.out.println("Strukturuotų atvejų testas: ");
        for (String i : inputs) {
            String h = MyHash.hash(i.getBytes(StandardCharsets.US_ASCII));
            String existing = seen.putIfAbsent(h, i);
            if (existing != null) {
                collisions++;
                System.out.println("Kolizija: | " + existing + " ir " + i);
            }
        }

        System.out.println("Struktūruotų įvesčių: " + inputs.length + ", kolizijų: " + collisions);
    }

    static void avalancheTest() {
        int[] lengths = {10, 100, 500, 1000};
        int pairsPerLength = 25000;
        Random rand = new Random(seed);

        double totalBitSum = 0;
        double totalHexSum = 0;
        double totalBitMin = 100;
        double totalBitMax = 0;
        double totalHexMin = 100;
        double totalHexMax = 0;

        System.out.println("Ilgis;MinBit%;MaxBit%;VidBit%;MinHex%;MaxHex%;VidHex%");

        for (int len : lengths) {
            double bitSum = 0;
            double hexSum = 0;

            double bitMin = 100;
            double bitMax = 0;

            double hexMin = 100;
            double hexMax = 0;

            for (int i = 0; i < pairsPerLength; i++) {
                String text1 = randomAscii(rand, len);
                char[] chars = text1.toCharArray();

                int position = rand.nextInt(len);
                char oldChar = chars[position];
                char newChar = randomChar(rand);

                while (oldChar == newChar) {
                    newChar = randomChar(rand);
                }
                chars[position] = newChar;
                String text2 = new String(chars);

                String hashA = MyHash.hash(text1.getBytes(StandardCharsets.US_ASCII));
                String hashB = MyHash.hash(text2.getBytes(StandardCharsets.US_ASCII));

                int bitDiff = countBitDiff(hashA, hashB);
                int hexDiff = countHexDiff(hashA, hashB);

                double bitPercent = 100.0 * bitDiff / (hashA.length() * 4);
                double hexPercent = 100.0 * hexDiff / hashA.length();

                bitSum += bitPercent;
                hexSum += hexPercent;
                bitMin = Math.min(bitMin, bitPercent);
                bitMax = Math.max(bitMax, bitPercent);
                hexMin = Math.min(hexMin,hexPercent);
                hexMax = Math.max(hexMax, hexPercent);

                System.err.println(len + ";" +String.format("%.2f", bitPercent));

            }

            System.out.printf("%d;%.1f;%.1f;%.2f;%.1f;%.1f;%.2f%n",
                    len, bitMin, bitMax, bitSum / pairsPerLength, hexMin, hexMax, hexSum / pairsPerLength);
            totalBitSum += bitSum;
            totalHexSum += hexSum;
            totalBitMin = Math.min(totalBitMin, bitMin);
            totalBitMax = Math.max(totalBitMax, bitMax);
            totalHexMin = Math.min(totalHexMin, hexMin);
            totalHexMax = Math.max(totalHexMax, hexMax);
        }
        int totalPairs = pairsPerLength * lengths.length;
        System.out.printf("Visi;%.1f;%.1f;%.2f;%.1f;%.1f;%.2f%n",
                totalBitMin, totalBitMax, totalBitSum / totalPairs,
                totalHexMin, totalHexMax, totalHexSum / totalPairs);

    }

    static void SaltTest() {
        String target = "5293";
        String targetHash = MyHash.hash(target.getBytes(StandardCharsets.US_ASCII));

        //be druskos
        //bandymai iki pirmo sutapimo
        int attempts = 0;
        List<String> matches = new ArrayList<>();
        List<Integer> when = new ArrayList<>();
        Double firstMatchTimeMs = -1.0;

        long start = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            String candidate = String.format("%04d", i);
            String hash = MyHash.hash(candidate.getBytes(StandardCharsets.US_ASCII));
            attempts++;
            if (hash.equals(targetHash)) {
                matches.add(candidate);
                when.add(attempts);

                if (firstMatchTimeMs == -1.0) {
                    long matchTime = System.nanoTime();
                    firstMatchTimeMs = (matchTime - start) /1000000.0;
                }

            }
        }
        long end = System.nanoTime();
        double ms = (end - start) / 1_000_000.0;

        System.out.println("Be druskos: ");
        System.out.println("Tikslinė Įvestis: " + target);
        System.out.println("Tikslinė maiša: " + targetHash);
        System.out.println("Bandymai iki pirmo atitikimo: " + when.get(0)) ;
        System.out.println("Laikas iki pirmo atitikimo: " + firstMatchTimeMs);
        System.out.println("Bandymai: " + attempts + ", laikas: " + ms + "ms");
        System.out.println("Sutampantys kandidatai: " + matches);

        //====================SU DRUSKA===============
        int attempts1 = 0;
        List<String> matches1 = new ArrayList<>();
        List<Integer> when1 = new ArrayList<>();

        String salt = randomSalt();
        String targetWithSalt = target + salt;
        String targetHash1 = MyHash.hash(targetWithSalt.getBytes(StandardCharsets.US_ASCII));

        double firstMatchTimeMs1 = -1.0;

        long start1 = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            String candidate = String.format("%04d", i);
            String candidateWithSalt = candidate + salt;
            String hash = MyHash.hash(candidateWithSalt.getBytes(StandardCharsets.US_ASCII));
            attempts1++;
            if (hash.equals(targetHash1)) {
                matches1.add(candidate);
                when1.add(attempts1);

                if (firstMatchTimeMs1 == -1.0) {
                    long matchTime1 = System.nanoTime();
                    firstMatchTimeMs1 = (matchTime1 - start1) / 1000000.0;
                }
            }

        }
        long end1 = System.nanoTime();
        double ms1 = (end1 - start1) / 1_000_000.0;

        System.out.println("Su druska: ");
        System.out.println("Tikslinė Įvestis: " + target);
        System.out.println("Druska: " + salt);
        System.out.println("Tikslinė maiša (su druska): " + targetHash1);
        System.out.println("Bandymai iki pirmo atitikimo: " + when1.get(0)) ;
        System.out.println("Laikas iki pirmo atitikimo: " + firstMatchTimeMs1);
        System.out.println("Bandymai: " + attempts1 + ", laikas: " + ms1 + "ms");
        System.out.println("Sutampantys kandidatai: " + matches1);

    }
    /* HELPERS */
    //grazinamas hash skirtumas bitais
    static int countBitDiff(String hexA, String hexB) {
        int diff = 0;
        for (int i = 0; i < hexA.length(); i += 2) {
            int byteA = Integer.parseInt(hexA.substring(i, i + 2), 16);
            int byteB = Integer.parseInt(hexB.substring(i, i + 2), 16);
            diff += Integer.bitCount(byteA ^ byteB);
        }
        return diff;
    }

    //grazinamas hash skirtumas hex
    static int countHexDiff(String hexA, String hexB) {
        int diff = 0;
        for (int i = 0; i < hexA.length(); i++) {
            if (hexA.charAt(i) != hexB.charAt(i)) {
                diff++;
            }
        }
        return diff;
    }

    //random char simbolio generavimas
    static char randomChar(Random rand) {
        return alphabet.charAt(rand.nextInt(alphabet.length()));
    }

    //Ascii eilues generavimas
    static String randomAscii(Random rand, int len) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < len; i++) {
            int index = rand.nextInt(alphabet.length());
            text.append(alphabet.charAt(index));
        }

        return text.toString();
    }

    //atsitiktinai generuojama druska
    static String randomSalt() {
        Random rand = new Random(seed);
        StringBuilder saltBuilder = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            saltBuilder.append(alphabet.charAt(rand.nextInt(alphabet.length())));
        }

        String salt = saltBuilder.toString();

        return salt;
    }
}
