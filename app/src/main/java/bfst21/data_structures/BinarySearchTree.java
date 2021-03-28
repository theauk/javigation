package bfst21.data_structures;

// Adapted from Algorithms 4th ed. p. 398-399
public class BinarySearchTree<Key extends Comparable<Key>, Value> {

    private BSTNode root;

    public int size() {
        return size(root);
    }

    private int size(BSTNode x) {
        if (x == null) return 0;
        else return x.N;
    }

    public Value get(Key key) {
        return get(root, key);
    }

    private Value get(BSTNode x, Key key) {
        if (x == null) return null;

        int compare = key.compareTo(x.key);
        if (compare < 0) {
            return get(x.left, key);
        } else if (compare > 0) {
            return get(x.right, key);
        } else {
            return x.val;
        }
    }

    public void put(Key key, Value val) {
        root = put(root, key, val);
    }

    private BSTNode put(BSTNode x, Key key, Value val) {
        if (x == null) return new BSTNode(key, val, 1);

        int compare = key.compareTo(x.key);
        if (compare < 0) {
            x.left = put(x.left, key, val);
        } else if (compare > 0) {
            x.right = put(x.right, key, val);
        } else {
            throw new RuntimeException("Keys are not unique");
        }
        x.N = size(x.left) + size(x.right) + 1;
        return x;
    }

    private class BSTNode {
        private Key key;
        private Value val;
        private BSTNode left, right;
        private int N;

        public BSTNode(Key key, Value val, int N) {
            this.key = key;
            this.val = val;
            this.N = N;
        }
    }
}
