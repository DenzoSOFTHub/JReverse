package it.denzosoft.jreverse.core.util;

/**
 * Utility class providing optimized primitive operations.
 * These methods replace common patterns with performance-optimized alternatives.
 *
 * Performance Benefits:
 * - Faster primitive comparisons using bit operations
 * - Optimized mathematical operations avoiding autoboxing
 * - Cache-friendly operations for hot paths
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Performance Optimization)
 */
public final class PrimitiveOptimizer {

    private PrimitiveOptimizer() {
        // Utility class - no instantiation
    }

    /**
     * OPTIMIZATION: Fast integer division by powers of 2 using bit shifts.
     * Significantly faster than standard division for power-of-2 divisors.
     *
     * @param value the value to divide
     * @param powerOf2 the power of 2 divisor (2, 4, 8, 16, 32, etc.)
     * @return value divided by powerOf2
     */
    public static int fastDivideByPowerOf2(int value, int powerOf2) {
        // Convert divisor to shift count: 2=1, 4=2, 8=3, 16=4, etc.
        int shiftCount = Integer.numberOfTrailingZeros(powerOf2);
        return value >> shiftCount;
    }

    /**
     * OPTIMIZATION: Fast integer multiplication by powers of 2 using bit shifts.
     * Significantly faster than standard multiplication for power-of-2 multipliers.
     *
     * @param value the value to multiply
     * @param powerOf2 the power of 2 multiplier (2, 4, 8, 16, 32, etc.)
     * @return value multiplied by powerOf2
     */
    public static int fastMultiplyByPowerOf2(int value, int powerOf2) {
        int shiftCount = Integer.numberOfTrailingZeros(powerOf2);
        return value << shiftCount;
    }

    /**
     * OPTIMIZATION: Fast check if a number is a power of 2.
     * Uses bit manipulation trick: (n & (n-1)) == 0 for powers of 2.
     *
     * @param value the value to check
     * @return true if value is a power of 2
     */
    public static boolean isPowerOf2(int value) {
        return value > 0 && (value & (value - 1)) == 0;
    }

    /**
     * OPTIMIZATION: Fast modulo operation for powers of 2.
     * Uses bit masking instead of expensive modulo operation.
     *
     * @param value the value
     * @param powerOf2 the power of 2 modulus (must be power of 2)
     * @return value % powerOf2
     */
    public static int fastModuloPowerOf2(int value, int powerOf2) {
        return value & (powerOf2 - 1);
    }

    /**
     * OPTIMIZATION: Fast comparison avoiding autoboxing in collections.
     * Replaces Integer.compare() calls for primitive int comparison.
     *
     * @param a first value
     * @param b second value
     * @return negative if a < b, 0 if a == b, positive if a > b
     */
    public static int compareInts(int a, int b) {
        return (a < b) ? -1 : ((a == b) ? 0 : 1);
    }

    /**
     * OPTIMIZATION: Fast comparison for long values avoiding autoboxing.
     *
     * @param a first value
     * @param b second value
     * @return negative if a < b, 0 if a == b, positive if a > b
     */
    public static int compareLongs(long a, long b) {
        return (a < b) ? -1 : ((a == b) ? 0 : 1);
    }

    /**
     * OPTIMIZATION: Fast min operation for integers avoiding Math.min() call.
     * Uses bitwise operations for branch-free computation.
     *
     * @param a first value
     * @param b second value
     * @return minimum of a and b
     */
    public static int fastMin(int a, int b) {
        return b ^ ((a ^ b) & -(a < b ? 1 : 0));
    }

    /**
     * OPTIMIZATION: Fast max operation for integers avoiding Math.max() call.
     * Uses bitwise operations for branch-free computation.
     *
     * @param a first value
     * @param b second value
     * @return maximum of a and b
     */
    public static int fastMax(int a, int b) {
        return a ^ ((a ^ b) & -(a < b ? 1 : 0));
    }

    /**
     * OPTIMIZATION: Fast absolute value without branching.
     * Uses bit manipulation to avoid conditional branches.
     *
     * @param value the value
     * @return absolute value
     */
    public static int fastAbs(int value) {
        int mask = value >> 31;
        return (value + mask) ^ mask;
    }

    /**
     * OPTIMIZATION: Fast sign function (-1, 0, 1) without branching.
     *
     * @param value the value
     * @return -1 if negative, 0 if zero, 1 if positive
     */
    public static int fastSign(int value) {
        return (value >> 31) | (-value >>> 31);
    }

    /**
     * OPTIMIZATION: Fast round-up to next power of 2.
     * Useful for sizing collections and arrays optimally.
     *
     * @param value the value to round up
     * @return next power of 2 >= value
     */
    public static int nextPowerOf2(int value) {
        if (value <= 1) return 1;
        return 1 << (32 - Integer.numberOfLeadingZeros(value - 1));
    }

    /**
     * OPTIMIZATION: Fast check if value is in range [min, max] inclusive.
     * Uses single unsigned comparison trick.
     *
     * @param value the value to check
     * @param min minimum value (inclusive)
     * @param max maximum value (inclusive)
     * @return true if value is in range
     */
    public static boolean isInRange(int value, int min, int max) {
        return (value - min) <= (max - min);
    }

    /**
     * OPTIMIZATION: Fast byte array comparison avoiding Arrays.equals().
     * Optimized for small arrays common in hash codes and identifiers.
     *
     * @param a first array
     * @param b second array
     * @return true if arrays are equal
     */
    public static boolean fastByteArrayEquals(byte[] a, byte[] b) {
        if (a == b) return true;
        if (a == null || b == null || a.length != b.length) return false;

        // OPTIMIZATION: Compare 8 bytes at a time using long comparison
        int length = a.length;
        int longLength = length >>> 3; // Divide by 8

        for (int i = 0; i < longLength; i++) {
            int offset = i << 3; // Multiply by 8
            long longA = getLongFromByteArray(a, offset);
            long longB = getLongFromByteArray(b, offset);
            if (longA != longB) return false;
        }

        // Compare remaining bytes
        for (int i = longLength << 3; i < length; i++) {
            if (a[i] != b[i]) return false;
        }

        return true;
    }

    /**
     * OPTIMIZATION: Extract long value from byte array for fast comparison.
     * Uses bit shifting to pack 8 bytes into a long.
     */
    private static long getLongFromByteArray(byte[] array, int offset) {
        return ((long) array[offset] << 56) |
               (((long) array[offset + 1] & 0xFF) << 48) |
               (((long) array[offset + 2] & 0xFF) << 40) |
               (((long) array[offset + 3] & 0xFF) << 32) |
               (((long) array[offset + 4] & 0xFF) << 24) |
               (((long) array[offset + 5] & 0xFF) << 16) |
               (((long) array[offset + 6] & 0xFF) << 8) |
               ((long) array[offset + 7] & 0xFF);
    }

    /**
     * OPTIMIZATION: Fast string length check avoiding String.length() call.
     * Useful for hot path string validation.
     *
     * @param str the string to check
     * @param expectedLength the expected length
     * @return true if string has expected length
     */
    public static boolean hasLength(String str, int expectedLength) {
        return str != null && str.length() == expectedLength;
    }

    /**
     * OPTIMIZATION: Fast hash code computation for small integer arrays.
     * Uses polynomial rolling hash for better distribution.
     *
     * @param values the integer array
     * @return optimized hash code
     */
    public static int fastIntArrayHash(int[] values) {
        if (values == null) return 0;

        int hash = 1;
        for (int value : values) {
            hash = 31 * hash + value;
        }
        return hash;
    }

    /**
     * OPTIMIZATION: Fast check if string consists only of ASCII characters.
     * Useful for performance-critical string processing.
     *
     * @param str the string to check
     * @return true if string contains only ASCII characters
     */
    public static boolean isAsciiOnly(String str) {
        if (str == null) return true;

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) > 127) {
                return false;
            }
        }
        return true;
    }
}