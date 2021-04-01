package bfst21.data_structures;

import java.util.ArrayList;
import java.util.List;


//TODO Adapted from Troels ???
public class BinarySearchTree<Value> {
    List<BSTNode> BSTNodes = new ArrayList<>();
    boolean sorted = true;

    public void put(long key, Value val) {
        BSTNodes.add(new BSTNode(key, val));
        sorted = false;
    }

    public Value get(long key) {
        if (!sorted) {
            BSTNodes.sort((a, b) -> Long.compare(a.key, b.key));
            sorted = true;
        }
        int lo = 0;
        int hi = BSTNodes.size();
        while (lo + 1 < hi) {
            int mid = (lo + hi) / 2;

            int compare = Long.compare(key,(BSTNodes.get(mid).key));
            if (compare < 0) {
                hi = mid;
            } else {
                lo = mid;
            }
        }
        BSTNode node = BSTNodes.get(lo);
        return Long.compare(node.key, key) == 0 ? node.val : null;
    }

    private class BSTNode {
        private final long key;
        private final Value val;

        public BSTNode(long key, Value val) {
            this.key = key;
            this.val = val;

        }

    }
}

