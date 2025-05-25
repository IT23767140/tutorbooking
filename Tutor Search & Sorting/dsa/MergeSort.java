package com.PGN24.tutorbooking.dsa;

import com.PGN24.tutorbooking.model.Tutor;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator; // For polymorphism in comparison logic

/**
 * Implements the Merge Sort algorithm for sorting a List of Tutor objects.
 * This class contains static methods and does not maintain state.
 * Demonstrates Polymorphism through the use of a Comparator to define sorting order.
 */
public class MergeSort {

    /**
     * Sorts a list of Tutors using the Merge Sort algorithm.
     * The original list is modified.
     *
     * @param listToSort The list of Tutor objects to be sorted.
     * @param comparator The Comparator used to determine the order of Tutors.
     * This allows for polymorphic sorting behavior.
     */
    public static void sort(List<Tutor> listToSort, Comparator<Tutor> comparator) {
        if (listToSort == null || listToSort.size() <= 1) {
            return; // Already sorted or nothing to sort
        }
        if (comparator == null) {
            throw new IllegalArgumentException("Comparator cannot be null for MergeSort");
        }

        // Create a temporary array for merging (or use a copy of the list)
        // For simplicity, we'll work on a copy and then clear/repopulate the original list.
        List<Tutor> tempList = new ArrayList<>(listToSort);
        mergeSortRecursive(tempList, 0, tempList.size() - 1, comparator);

        // Copy the sorted elements back to the original list
        listToSort.clear();
        listToSort.addAll(tempList);
    }

    /**
     * Recursive helper method for Merge Sort.
     *
     * @param list The list (or sublist) to sort.
     * @param left The starting index of the sublist.
     * @param right The ending index of the sublist.
     * @param comparator The Comparator for comparing Tutors.
     */
    private static void mergeSortRecursive(List<Tutor> list, int left, int right, Comparator<Tutor> comparator) {
        if (left < right) {
            int middle = left + (right - left) / 2; // Avoids potential overflow for large left/right

            // Sort first and second halves
            mergeSortRecursive(list, left, middle, comparator);
            mergeSortRecursive(list, middle + 1, right, comparator);

            // Merge the sorted halves
            merge(list, left, middle, right, comparator);
        }
    }

    /**
     * Merges two sorted sub-arrays of the list.
     * First sub-array is list[left..middle]
     * Second sub-array is list[middle+1..right]
     *
     * @param list The main list containing the sub-arrays.
     * @param left The starting index of the first sub-array.
     * @param middle The ending index of the first sub-array.
     * @param right The ending index of the second sub-array.
     * @param comparator The Comparator for comparing Tutors.
     */
    private static void merge(List<Tutor> list, int left, int middle, int right, Comparator<Tutor> comparator) {
        // Sizes of the two sub-arrays to be merged
        int n1 = middle - left + 1;
        int n2 = right - middle;

        // Create temporary lists (or arrays)
        List<Tutor> leftSublist = new ArrayList<>(n1);
        List<Tutor> rightSublist = new ArrayList<>(n2);

        // Copy data to temporary lists
        for (int i = 0; i < n1; ++i) {
            leftSublist.add(list.get(left + i));
        }
        for (int j = 0; j < n2; ++j) {
            rightSublist.add(list.get(middle + 1 + j));
        }

        // Merge the temporary lists back into the original list[left..right]

        int i = 0; // Initial index of first sub-array
        int j = 0; // Initial index of second sub-array
        int k = left; // Initial index of merged sub-array

        while (i < n1 && j < n2) {
            // Use the comparator to decide which element comes first
            if (comparator.compare(leftSublist.get(i), rightSublist.get(j)) <= 0) {
                list.set(k, leftSublist.get(i));
                i++;
            } else {
                list.set(k, rightSublist.get(j));
                j++;
            }
            k++;
        }

        // Copy remaining elements of leftSublist, if any
        while (i < n1) {
            list.set(k, leftSublist.get(i));
            i++;
            k++;
        }

        // Copy remaining elements of rightSublist, if any
        while (j < n2) {
            list.set(k, rightSublist.get(j));
            j++;
            k++;
        }
    }
}
