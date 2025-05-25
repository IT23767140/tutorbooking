package com.PGN24.tutorbooking.dsa;

import com.PGN24.tutorbooking.model.Tutor;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator; // For polymorphism in comparison logic

/**
 * Implements a Binary Search Tree (BST) to store and manage Tutor objects.
 * Demonstrates Encapsulation by hiding the internal root and node structure.
 * Demonstrates Polymorphism through the use of a Comparator to define ordering.
 */
public class BinarySearchTree {
    private BSTNode root; // Encapsulated root of the BST
    private final Comparator<Tutor> comparator; // Defines how Tutors are ordered in the BST

    /**
     * Constructor for BinarySearchTree.
     * @param comparator The Comparator used to order Tutors in the tree.
     * This allows the BST to be polymorphic regarding its ordering strategy.
     */
    public BinarySearchTree(Comparator<Tutor> comparator) {
        this.root = null;
        if (comparator == null) {
            throw new IllegalArgumentException("Comparator cannot be null for BinarySearchTree");
        }
        this.comparator = comparator;
    }

    /**
     * Inserts a Tutor into the BST.
     * If a Tutor that is considered "equal" by the comparator already exists,
     * the new Tutor is not inserted (or could be updated, depending on desired behavior).
     * @param tutor The Tutor object to insert.
     */
    public void insert(Tutor tutor) {
        if (tutor == null) {
            return; // Or throw IllegalArgumentException
        }
        this.root = insertRecursive(this.root, tutor);
    }

    /**
     * Recursive helper method to insert a Tutor.
     * @param current The current node in the traversal.
     * @param tutor The Tutor to insert.
     * @return The root of the modified subtree.
     */
    private BSTNode insertRecursive(BSTNode current, Tutor tutor) {
        if (current == null) {
            return new BSTNode(tutor);
        }

        int compareResult = comparator.compare(tutor, current.data);

        if (compareResult < 0) {
            current.left = insertRecursive(current.left, tutor);
        } else if (compareResult > 0) {
            current.right = insertRecursive(current.right, tutor);
        }
        // If compareResult is 0, tutor is "equal" to current.data.
        // For this implementation, we don't insert duplicates based on the comparator.
        // Alternatively, you could update the existing node or allow duplicates.

        return current;
    }

    /**
     * Searches for a Tutor in the BST based on the criteria defined by the comparator.
     * @param tutorToFind A Tutor object containing the key values to search for.
     * The comparator will use this object to find a match.
     * @return The found Tutor object, or null if not found.
     */
    public Tutor search(Tutor tutorToFind) {
        if (tutorToFind == null) {
            return null;
        }
        return searchRecursive(this.root, tutorToFind);
    }

    /**
     * Recursive helper method to search for a Tutor.
     * @param current The current node in the traversal.
     * @param tutorToFind The Tutor to find.
     * @return The found Tutor, or null.
     */
    private Tutor searchRecursive(BSTNode current, Tutor tutorToFind) {
        if (current == null) {
            return null; // Tutor not found
        }

        int compareResult = comparator.compare(tutorToFind, current.data);

        if (compareResult == 0) {
            return current.data; // Tutor found
        } else if (compareResult < 0) {
            return searchRecursive(current.left, tutorToFind);
        } else {
            return searchRecursive(current.right, tutorToFind);
        }
    }

    /**
     * Performs an in-order traversal of the BST.
     * This will return the Tutors in the order defined by the Comparator.
     * @return A List of Tutors in sorted order.
     */
    public List<Tutor> getInOrderTraversal() {
        List<Tutor> result = new ArrayList<>();
        inOrderRecursive(this.root, result);
        return result;
    }

    /**
     * Recursive helper method for in-order traversal.
     * @param node The current node.
     * @param resultList The list to add Tutors to.
     */
    private void inOrderRecursive(BSTNode node, List<Tutor> resultList) {
        if (node != null) {
            inOrderRecursive(node.left, resultList);
            resultList.add(node.data);
            inOrderRecursive(node.right, resultList);
        }
    }

    /**
     * Deletes a Tutor from the BST.
     * (This is a more complex operation and is provided as a complete example)
     * @param tutorToDelete The Tutor object to delete. The comparator is used to find it.
     */
    public void delete(Tutor tutorToDelete) {
        if (tutorToDelete == null) {
            return;
        }
        this.root = deleteRecursive(this.root, tutorToDelete);
    }

    /**
     * Recursive helper method to delete a Tutor.
     * @param current The current node in the traversal.
     * @param tutorToDelete The Tutor to delete.
     * @return The root of the modified subtree.
     */
    private BSTNode deleteRecursive(BSTNode current, Tutor tutorToDelete) {
        if (current == null) {
            return null; // Tutor not found
        }

        int compareResult = comparator.compare(tutorToDelete, current.data);

        if (compareResult < 0) {
            current.left = deleteRecursive(current.left, tutorToDelete);
        } else if (compareResult > 0) {
            current.right = deleteRecursive(current.right, tutorToDelete);
        } else {
            // Node to delete found

            // Case 1: Node has no children or only one child
            if (current.left == null) {
                return current.right;
            } else if (current.right == null) {
                return current.left;
            }

            // Case 2: Node has two children
            // Find the in-order successor (smallest value in the right subtree)
            current.data = findMinValue(current.right);

            // Delete the in-order successor from the right subtree
            current.right = deleteRecursive(current.right, current.data); // current.data is now the successor
        }
        return current;
    }

    /**
     * Finds the Tutor with the minimum value in a subtree (based on the comparator).
     * This is the leftmost node in the subtree.
     * @param node The root of the subtree.
     * @return The Tutor object with the minimum value.
     */
    private Tutor findMinValue(BSTNode node) {
        Tutor minValue = node.data;
        while (node.left != null) {
            minValue = node.left.data;
            node = node.left;
        }
        return minValue;
    }

    /**
     * Clears all elements from the BST.
     */
    public void clear() {
        this.root = null;
        // Java's garbage collector will handle the deallocation of nodes.
    }

    /**
     * Checks if the BST is empty.
     * @return true if the BST contains no Tutors, false otherwise.
     */
    public boolean isEmpty() {
        return this.root == null;
    }
}
