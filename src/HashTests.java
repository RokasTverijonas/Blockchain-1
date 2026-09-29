import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class HashTests {
    //ascii simboliain skirti teksto generavimui
    private final static String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final static int seed = 283;

    public static void main(String[] args) throws Exception {
        //formatTest();
        //determinismTest();
        //efficiencyTest();
        collisionTest();;
        structuredCollisionTest();
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
        System.out.println("A2: " + a);


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

    //generavimas
    static String randomAscii(Random rand, int len) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < len; i++) {
            int index = rand.nextInt(alphabet.length());
            text.append(alphabet.charAt(index));
        }

        return text.toString();
    }
}
