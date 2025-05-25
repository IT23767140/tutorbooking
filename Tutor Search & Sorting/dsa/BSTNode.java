package com.PGN24.tutorbooking.dsa;

import com.PGN24.tutorbooking.model.Tutor; // Assuming Tutor model is in this package

/**
 * Represents a node in the Binary Search Tree.
 * This is a helper class for BinarySearchTree and typically has package-private or protected access
 * if not intended for use outside the dsa package directly.
 * For simplicity and direct use by BinarySearchTree, fields can be directly accessed by it.
 */
class BSTNode {
    Tutor data;      // The data stored in the node (a Tutor object)
    BSTNode left;    // Reference to the left child node
    BSTNode right;   // Reference to the right child node

    /**
     * Constructor to create a new BSTNode.
     * @param tutor The Tutor object to be stored in this node.
     */
    public BSTNode(Tutor tutor) {
        this.data = tutor;
        this.left = null;
        this.right = null;
    }

    // Getters can be added if stricter encapsulation is needed for external access,
    // but for internal use by BinarySearchTree, direct field access is common.
    public Tutor getData() {
        return data;
    }

    public BSTNode getLeft() {
        return left;
    }

    public BSTNode getRight() {
        return right;
    }

    // Setters can also be added if needed.
    public void setData(Tutor data) {
        this.data = data;
    }

    public void setLeft(BSTNode left) {
        this.left = left;
    }

    public void setRight(BSTNode right) {
        this.right = right;
    }
}