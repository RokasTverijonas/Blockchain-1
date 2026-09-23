import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        byte[] input;

        if (args.length > 0) {
            System.out.println("Naudojamas failo įvesties režimas.");


            try {
                input = readFile(args[0]);
            } catch (IOException e) {
                System.out.println("Nepavyko perskaityti failo.");
                return;
            } catch (InvalidPathException e) {
                System.out.println("Neteisingas failo kelias.");
                return;
            }
        } else {
            System.out.println("Naudojamas rankinės įvesties režimas.");

            String text = userInput(sc);
            input = text.getBytes(StandardCharsets.UTF_8);
        }

        String result = MyHash.hash(input);

        System.out.println("Hash: " + result);
    }

    private static String userInput(Scanner sc) {
        System.out.println("Įveskite tekstą: ");
        return sc.nextLine();
    }

    private static byte[] readFile(String fileName) throws IOException {
        Path path = Path.of(fileName);

        return Files.readAllBytes(path);
    }

}

