import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ShamirSecretSharing {

    private static final SecureRandom random = new SecureRandom();
    private static final BigInteger PRIME_MODULUS = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16); // A large prime modulus

    // Method to split the secret into n shares using k threshold
    public static List<Share> splitSecret(BigInteger secret, int n, int k) {
        List<BigInteger> coefficients = new ArrayList<>();
        // Generate random coefficients for the polynomial
        for (int i = 0; i < k - 1; i++) {
            coefficients.add(new BigInteger(secret.bitLength(), random).mod(PRIME_MODULUS));
        }

        List<Share> shares = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            BigInteger x = BigInteger.valueOf(i);
            BigInteger y = evaluatePolynomial(secret, coefficients, x);
            shares.add(new Share(x, y));
        }

        return shares;
    }

    // Method to evaluate the polynomial at a given x
    private static BigInteger evaluatePolynomial(BigInteger secret, List<BigInteger> coefficients, BigInteger x) {
        BigInteger result = secret;
        BigInteger xPower = BigInteger.ONE;

        for (BigInteger coefficient : coefficients) {
            xPower = xPower.multiply(x).mod(PRIME_MODULUS);
            result = result.add(coefficient.multiply(xPower)).mod(PRIME_MODULUS);
        }

        return result;
    }

    // Method to reconstruct the secret using k shares
    public static BigInteger reconstructSecret(List<Share> shares, int k) {
        BigInteger secret = BigInteger.ZERO;

        for (int i = 0; i < k; i++) {
            BigInteger xi = shares.get(i).getX();
            BigInteger yi = shares.get(i).getY();

            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < k; j++) {
                if (i != j) {
                    BigInteger xj = shares.get(j).getX();
                    numerator = numerator.multiply(xj.negate()).mod(PRIME_MODULUS);
                    denominator = denominator.multiply(xi.subtract(xj)).mod(PRIME_MODULUS);
                }
            }

            BigInteger lagrange = numerator.multiply(denominator.modInverse(PRIME_MODULUS)).mod(PRIME_MODULUS);
            secret = secret.add(yi.multiply(lagrange)).mod(PRIME_MODULUS);
        }

        return secret.mod(PRIME_MODULUS);
    }

    // Share class to hold the x and y values of each share
    public static class Share {
        private final BigInteger x;
        private final BigInteger y;

        public Share(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }

        public BigInteger getX() {
            return x;
        }

        public BigInteger getY() {
            return y;
        }

        @Override
        public String toString() {
            return "Share{" + "x=" + x + ", y=" + y + '}';
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Prompt user for n and k
        System.out.print("Enter the total number of shares (n): ");
        int n = scanner.nextInt();

        System.out.print("Enter the threshold number of shares (k): ");
        int k = scanner.nextInt();

        List<Share> shares = new ArrayList<>();

        // Prompt user for each share's base and value
        for (int i = 1; i <= n; i++) {
            System.out.print("Enter the base for share " + i + ": ");
            int base = scanner.nextInt();

            System.out.print("Enter the value for share " + i + ": ");
            String value = scanner.next();

            // Convert the base and value to BigInteger and add to shares
            BigInteger x = BigInteger.valueOf(i);  // Share index is x value
            BigInteger y = new BigInteger(value, base);
            shares.add(new Share(x, y));
        }

        // Reconstruct the secret using the first k shares
        List<Share> usedShares = shares.subList(0, k); // Using first k shares
        BigInteger reconstructedSecret = reconstructSecret(usedShares, k);

        // Output the reconstructed secret
        System.out.println("Reconstructed Secret: " + reconstructedSecret);

        scanner.close();
    }
}
