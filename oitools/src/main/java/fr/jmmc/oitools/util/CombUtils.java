/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.util;

import java.util.ArrayList;
import java.util.List;

/**
 * This class gathers several combinatory algorithms
 * @author bourgesl
 */
public final class CombUtils {

    /**
     * Forbidden constructor
     */
    private CombUtils() {
        // no-op
    }

    /**
     * Return factorial(n) = n!
     * @param n integer
     * @return factorial(n)
     */
    public static int fact(final int n) {
        int res = 1;
        for (int i = 1; i <= n; i++) {
            res *= i;
        }
        return res;
    }

    /**
     * Return the number of arrangements without repetition
     * @param n number of elements
     * @param k number of items to choose
     * @return number of arrangements
     */
    public static int arr(final int n, final int k) {
        int res = 1;

        // A-n-k = n! / (n - k)!
        for (int i = n, min = n - k + 1; i >= min; i--) {
            res *= i;
        }
        return res;
    }

    /**
     * Return the number of combinations (no repetition, no ordering)
     * @param n number of elements
     * @param k number of items to choose
     * @return number of generateCombinations
     */
    public static int comb(final int n, final int k) {
        //C-n-k = A-n-k/k!
        return arr(n, k) / fact(k);
    }

    /**
     * Generate all combinations (no repetition, no ordering)
     * @param n number of elements
     * @param k number of items to choose
     * @return list of all combinations (integer arrays)
     */
    public static List<int[]> generateCombinations(final int n, final int k) {
        final int count = comb(n, k);

        final List<int[]> results = new ArrayList<int[]>(count);

        recursiveCombinations(n, k, results, new int[k], 0, 0);

        return results;
    }

    /**
     * Recursive algorithm to generate all combinations
     * @param n number of elements
     * @param k number of items to choose
     * @param results result array
     * @param current current array
     * @param position position in the array
     * @param nextInt next integer value
     */
    private static void recursiveCombinations(final int n, final int k, final List<int[]> results, final int[] current, final int position, final int nextInt) {
        for (int i = nextInt; i < n; i++) {
            current[position] = i;

            if (position + 1 == k) {
                // copy current result :
                final int[] res = new int[k];
                System.arraycopy(current, 0, res, 0, k);
                results.add(res);
            } else {
                recursiveCombinations(n, k, results, current, position + 1, i + 1);
            }
        }
    }

    /**
     * Generate all permutations (no repetition, but ordering)
     * @param n number of elements
     * @return list of all permutations (integer arrays)
     */
    public static List<int[]> generatePermutations(final int n) {

        final int[] input = new int[n];
        for (int i = 0; i < n; i++) {
            input[i] = i;
        }
        final int count = fact(n);

        final List<int[]> results = new ArrayList<int[]>(count);

        recursivePermutations(0, n, input, results);

        return results;
    }

    /**
     * Recursive algorithm to generate all permutations
     * @param offset position in the array
     * @param len input array length
     * @param input input array
     * @param output output list
     */
    private static void recursivePermutations(final int offset, final int len, final int[] input, final List<int[]> output) {
        if (len - offset == 1) {
            // Input now contains a permutation, here I store it in output,
            // but you can do anything you like with it
            final int[] o = new int[len];
            System.arraycopy(input, 0, o, 0, len);

            output.add(o);
            return;
        }

        final int a = input[offset];
        int b;

        for (int i = offset; i < len; i++) {
            // Swap elements
            b = input[i];
            input[i] = a;
            input[offset] = b;

            recursivePermutations(offset + 1, len, input, output);

            // Restore element
            input[i] = b;
        }

        input[offset] = a;
    }

    /**
     * Return the number of n-tuples (repetition allowed)
     * @param n number of elements
     * @param k number of items to choose
     * @return number of n-tuples
     */
    public static int tuples(final int n, final int k) {
        int res = 1;
        for (int i = 0; i < k; i++) {
            res *= n;
        }
        return res;
    }

    /**
     * Generate all n-tuples (repetition allowed)
     * @param n number of elements
     * @param k number of items to choose
     * @return list of all n-tuples (integer arrays)
     */
    public static List<int[]> generateTuples(final int n, final int k) {
        final int count = tuples(n, k);

        final List<int[]> results = new ArrayList<int[]>(count);

        recursiveTuples(n, k, results, new int[k], 0, 0);

        return results;
    }

    /**
     * Recursive algorithm to generate all n-tuples
     * @param n number of elements
     * @param k number of items to choose
     * @param results result array
     * @param current current array
     * @param position position in the array
     * @param nextInt next integer value
     */
    private static void recursiveTuples(final int n, final int k, final List<int[]> results, final int[] current, final int position, final int nextInt) {
        for (int i = nextInt; i < n; i++) {
            current[position] = i;

            if (position + 1 == k) {
                // copy current result :
                final int[] res = new int[k];
                System.arraycopy(current, 0, res, 0, k);
                results.add(res);
            } else {
                recursiveTuples(n, k, results, current, position + 1, 0);
            }
        }
    }
}
