package bfst21.data_structures;

import java.util.ArrayList;
import java.util.List;


//TODO Adapted from Troels ???
public class AlternateBinarySearchTree<Key extends Comparable<Key>, Value> {
    List<BSTNode> BSTNodes = new ArrayList<>();
    boolean sorted = true;

    public void put(Key key, Value val) {
        BSTNodes.add(new BSTNode(key, val));
        sorted = false;
    }

    public Value get(Key key) {
        if (!sorted) {
            BSTNodes.sort((a, b) -> a.key.compareTo(b.key));
            sorted = true;
        }
        int lo = 0;
        int hi = BSTNodes.size();
        while (lo + 1 < hi) {
            int mid = (lo + hi) / 2;

            int compare = key.compareTo(BSTNodes.get(mid).key);
            if (compare < 0) {
                hi = mid;
            } else {
                lo = mid;
            }
        }
        BSTNode node = BSTNodes.get(lo);
        return node.key.compareTo(key) == 0 ? node.val : null;
    }

    private class BSTNode {
        private Key key;
        private Value val;

        public BSTNode(Key key, Value val) {
            this.key = key;
            this.val = val;

        }
    }
}

