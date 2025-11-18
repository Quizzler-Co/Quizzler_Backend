import java.util.Scanner;

public class RunnerTemplate {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder input = new StringBuilder();
        
        while (scanner.hasNextLine()) {
            input.append(scanner.nextLine());
            if (scanner.hasNextLine()) {
                input.append("\n");
            }
        }
        
        String result = UserSolution.solve(input.toString());
        System.out.print(result);
        scanner.close();
    }
}

