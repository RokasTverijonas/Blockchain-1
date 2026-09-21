import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int choice = chooseMode(sc);
        sc.nextLine();

        byte[] input;

        if(choice == 1) {
            String text = userInput(sc);
            input = text.getBytes(StandardCharsets.UTF_8);
        } else {
            System.out.println("Įveskite failo pavadinimą: ");
            String fileName = sc.nextLine();

            try {
                input = readFile(fileName);
            } catch (IOException e) {
                System.out.println("Nepavyko perskaityti failo.");
                return;
            }
        }

        int result = MyHash.hash(input);

        System.out.println("Hash: " + Integer.toHexString(result));
    }

    private static int chooseMode(Scanner sc) {
        System.out.println("Pasirinkite įvesties režimą: ");
        System.out.println(" 1 - Įvedimas ranka;");
        System.out.println(" 2 - Failo skaitymas;");

        int choice = sc.nextInt();

        while (choice != 1 && choice != 2) {
            System.out.println("Neteisingas pasirinkimas. Įveskite 1 arba 2: ");
            choice = sc.nextInt();
        }

        return choice;
    }

    private static String userInput(Scanner sc) {
        System.out.println("Įveskite tekstą: ");

        return sc.nextLine();
    }

    private static byte[] readFile(String fileName) throws IOException {
        Path path = Path.of("C:\\Users\\tveri\\OneDrive\\Stalinis kompiuteris\\java\\MyHash\\" + fileName);

        return Files.readAllBytes(path);
    }

}

