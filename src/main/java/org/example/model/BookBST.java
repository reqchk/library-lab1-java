package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class BookBST {
    private class Node {
        String key;
        List<Book> books;
        Node left, right;

        Node(String key) {
            this.key = key;
            this.books = new ArrayList<>();
        }
    }

    private Node root;

    public void insert(Book book) {
        if (book == null || book.getTitle() == null) return;
        root = insertRec(root, book);
    }

    private Node insertRec(Node node, Book book) {
        if (node == null) {
            Node newNode = new Node(book.getTitle());
            newNode.books.add(book);
            return newNode;
        }
        int cmp = book.getTitle().compareToIgnoreCase(node.key);
        if (cmp < 0) { node.left = insertRec(node.left, book); }
        else if (cmp > 0) { node.right = insertRec(node.right, book); }
        else { node.books.add(book); }

        return node;
    }

    public List<Book> getAllBooks() {
        List<Book> result = new ArrayList<>();
        inOrderTraversal(root, result);
        return result;
    }

    private void inOrderTraversal(Node node, List<Book> result) {
        if (node != null) {
            inOrderTraversal(node.left, result);
            result.addAll(node.books);
            inOrderTraversal(node.right, result);
        }
    }

    public List<Book> searchByTitle(String title) {
        Node node = searchRec(root, title);
        return node != null ? node.books : new ArrayList<>();
    }

    private Node searchRec(Node node, String title) {
        if (node == null) return null;

        int cmp = title.compareToIgnoreCase(node.key);
        if (cmp < 0) return searchRec(node.left, title);
        if (cmp > 0) return searchRec(node.right, title);

        return node;
    }

    public void remove(Book book) {
        if (book == null || book.getTitle() == null) return;
        root = removeRec(root, book);
    }

    private Node removeRec(Node node, Book book) {
        if (node == null) return null;

        int cmp = book.getTitle().compareToIgnoreCase(node.key);
        if (cmp < 0) { node.left = removeRec(node.left, book); }
        else if (cmp > 0) { node.right = removeRec(node.right, book); }
        else {
            node.books.remove(book);
            if (node.books.isEmpty()) {
                if (node.left == null) return node.right;
                if (node.right == null) return node.left;

                Node minNode = findMin(node.right);
                Node newNode = new Node(minNode.key);
                newNode.books = new ArrayList<>(minNode.books);
                newNode.left = node.left;
                newNode.right = removeRec(node.right, minNode.books.get(0));
                return newNode;
            }
        }
        return node;
    }

    private Node findMin(Node node) {
        while (node.left != null) node = node.left;
        return node;
    }
}