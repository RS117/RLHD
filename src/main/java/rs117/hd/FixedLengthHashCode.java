package rs117.hd;

public class FixedLengthHashCode {
    private final int[] coefficients;
    private final int seed;

    FixedLengthHashCode(int size) {
        coefficients = new int[size];
        coefficients[size - 1] = 1;
        for (int i = size - 2; i >= 0; --i) {
            coefficients[i] = 31 * coefficients[i + 1];
        }
        seed = 31 * coefficients[0];
    }

    public int hashCode(int[] data) {
        int result = seed;
        for (int i = 0; i < data.length && i < coefficients.length; ++i) {
            result += coefficients[i] * data[i];
        }
        return result;
    }
}
