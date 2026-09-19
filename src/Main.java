import java.nio.charset.StandardCharsets;

public class Main {
    
    public static void main(String[] args) {
        String input = "hello";
        int result = MyHash.hash(input.getBytes(StandardCharsets.UTF_8));
        System.out.println(Integer.toHexString(result));
    }
}
