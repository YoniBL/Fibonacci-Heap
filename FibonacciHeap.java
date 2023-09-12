import java.util.Arrays;

/**
 * FibonacciHeap
 *
 * An implementation of a Fibonacci Heap over integers.
 */
public class FibonacciHeap {
//    fields for Fibonacci heap

    private HeapNode first;
    private HeapNode last;
    private HeapNode min;
    private int size;
    private int marked_cnt;
    private int tree_cnt;

    private int max_rank;
    private static int link_cnt;
    private static int cuts_cnt;

    /**
     * public boolean isEmpty()
     * <p>
     * Returns true if and only if the heap is empty.
     * time complexity O(1)
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * public HeapNode insert(int key)
     * <p>
     * Creates a node (of type HeapNode) which contains the given key, and inserts it into the heap.
     * The added key is assumed not to already belong to the heap.
     * <p>
     * Returns the newly created node.
     * time complexity O(1)
     */
    public HeapNode insert(int key) {
        HeapNode new_first = new HeapNode(key);
        this.nodeInsert(new_first);
        size++;
        return new_first;
    }

    private void nodeInsert(HeapNode node){
        if (this.first != null) {
            HeapNode prev_first = this.first;
            node.next = prev_first;
            prev_first.prev = node;
            this.first = node;
            this.last.next = node;
            node.prev = this.last;
            if (this.min.getKey() > node.key) {
                this.min = node;
            }
        } else {
            this.first = node;
            this.last = node;
            this.min = node;
        }
        tree_cnt++;
    }



    /**
     * public void deleteMin()
     * <p>
     * Deletes the node containing the minimum key.
     * Time Complexity O(n) worst case, O(log(n)) amortized
     */
    public void deleteMin() {
        if (!isEmpty()) {
            this.delete(this.min);
        }
    }

    /**
     * public HeapNode findMin()
     * <p>
     * Returns the node of the heap whose key is minimal, or null if the heap is empty.
     */
    public HeapNode findMin() {
        return this.min;
    }

    public HeapNode getLast() {
        return this.last;
    }

    public HeapNode getFirst() {
        return this.first;
    }

    public int getMarkedCount(){
        return marked_cnt;
    }

    public int getTreeCount(){
        return tree_cnt;
    }

    /**
     * public void meld (FibonacciHeap heap2)
     * <p>
     * time complexity O(1)
     * Melds heap2 with the current heap.
     */
    public void meld(FibonacciHeap heap2) {
        if (!this.isEmpty()) {
            if (!heap2.isEmpty()){
                HeapNode heap1last = this.last;
                HeapNode heap2last = heap2.getLast();
                HeapNode heap1first = this.first;
                HeapNode heap2first = heap2.getFirst();
                heap1first.prev = heap2last;
                heap2last.setNext(heap1first);
                heap1last.next = heap2first;
                heap2first.setPrev(heap1last);
                if ( heap2.findMin().getKey()  < this.min.getKey() ){
                    this.min = heap2.findMin();
                }
                this.last = heap2last;
                size += heap2.size();
                marked_cnt += heap2.getMarkedCount();
                tree_cnt += heap2.getTreeCount();
            }
        }
        else {
            this.first = heap2.getFirst();
            this.last = heap2.getLast();
            this.size = heap2.size();
            this.marked_cnt = heap2.getMarkedCount();
            this.tree_cnt = heap2.getTreeCount();
            this.min = heap2.findMin();
        }


    }


    /**
     * public int size()
     * <p>
     * time complexity O(1)
     * Returns the number of elements in the heap.
     */
    public int size() {
        return this.size; // should be replaced by student code
    }

    /**
     * public int[] countersRep()
     * <p>
     * Return an array of counters. The i-th entry contains the number of trees of order i in the heap.
     * (Note: The size of of the array depends on the maximum order of a tree.)
     */
    public int[] countersRep() {
        int[] arr = new int[this.size];
        if (isEmpty()) {
            return arr;
        }
        HeapNode curr = this.first;
        int curr_max = 0;
        do {
            int index = curr.rank;
            arr[index]++;
            if(index > curr_max){
                curr_max = index;
            }
            curr = curr.next;
        }
        while (curr != this.first);
        return Arrays.copyOfRange(arr,0,curr_max+1); //	 to be replaced by student code
    }

    /**
     * public void delete(HeapNode x)
     * <p>
     * Deletes the node x from the heap.
     * It is assumed that x indeed belongs to the heap.
     */
    public void delete(HeapNode x) {
        if (this.size == 1) {
            this.first = null;
            this.last = null;
            this.size = 0;
            this.marked_cnt = 0;
            this.tree_cnt = 0;
            this.min = null;
        }
        else {
            if (x.parent != null) {
                this.cascadingCuts(x);
                this.delete(x); // x is now the first tree in the heap, and we will delete it
                    // (not coming back to this case, so the recursive call only happens once)
                size++; // because otherwise we -- twice bc of the recursive call
            } else {
                tree_cnt--;
                if (x.child == null) {
                    x.prev.next = x.next;
                    x.next.prev = x.prev;
                } else {
//                    if (x.next == x) {
//                        this.first = null;
//                    }why is this even here?
//                    if (x == this.first) {
//                        this.first = x.next;
//                    } I think this was here bc we had to cut but that was wrong so this causes other problems

                    // maya changed this to fit with the clarifications for delete-min in the pdf, guess we missed them
                    HeapNode child_to_disconnect = x.child;
                    if (x == this.first) {
                        this.first = child_to_disconnect;
                    }
                    if ( x == this.last) {
                        this.last = child_to_disconnect.prev;
                    }
                    cutAddToMiddleOfList(child_to_disconnect);
                    tree_cnt += x.rank;
                    // end of change
                }
            }
            if (x == this.first) {
                this.first = this.first.next;
            }
            if (x == this.last) {
                this.last = this.last.prev;
            }
            this.size--;
            this.consolidate();
        }
    }
    private void cutAddToMiddleOfList(HeapNode child_to_disconnect) {
        HeapNode parent = child_to_disconnect.parent;
        parent.prev.next = child_to_disconnect;
        parent.next.prev = child_to_disconnect.prev;
        child_to_disconnect.prev.next = parent.next;
        child_to_disconnect.prev = parent.prev;
        for (int i = 0; i < parent.rank; i++) {
            child_to_disconnect.parent= null;
            if (child_to_disconnect.mark && parent.parent == null) { // we are making this node a root, so it can't be marked
                child_to_disconnect.mark = false;
                marked_cnt--;
            }
            child_to_disconnect =  child_to_disconnect.next;
            //cuts_cnt++; ????????? does this count as a cut? I think it doesn't
        }
    }

    /**
     * public void decreaseKey(HeapNode x, int delta)
     * <p>
     * Decreases the key of the node x by a non-negative value delta. The structure of the heap should be updated
     * to reflect this change (for example, the cascading cuts procedure should be applied if needed).
     */
    public void decreaseKey(HeapNode x, int delta) {
        int new_key = x.getKey() - delta;
        if (x.parent != null) {
            if (x.parent.getKey() > new_key) {
                cascadingCuts(x);
            }
        }
        if (new_key < this.min.getKey()) {
            this.min = x;
        }
        x.key = new_key;
    }

    /**
     * public int nonMarked()
     * <p>
     * This function returns the current number of non-marked items in the heap
     */
    public int nonMarked() {
        return this.size - this.marked_cnt;
    }

    /**
     * public int potential()
     * <p>
     * This function returns the current potential of the heap, which is:
     * Potential = #trees + 2*#marked
     * <p>
     * In words: The potential equals to the number of trees in the heap
     * plus twice the number of marked nodes in the heap.
     */
    public int potential() {
        return this.tree_cnt + 2*(this.marked_cnt); // should be replaced by student code
    }

    private void cascadingCuts(HeapNode node) {
        HeapNode parent = node.parent;
        if (parent != null) {
            cut(node);
            if (parent.parent != null){
                if (!parent.mark){
                    parent.mark = true;
                    marked_cnt++;
                }
                else {
                    this.cascadingCuts(parent);
                }
            }
        }
    }

    /**
     * public void consolidate()
     * This function preforms a series of links to create a heap with at most one tree of each rank
     * Helper function for Delete-Min
     * Time Complexity: O(#trees + log(n)) = O(n) worst case
     */

    private void consolidate(){
        HeapNode[] buckets = new HeapNode[(int) Math.ceil(Math.log(this.size)/Math.log(2)) + 1]; //creating an array of "buckets"
                                            // to keep track of which rank of trees we found already
        HeapNode curr = first;
        HeapNode root;
        // iterating over roots of all the trees in the heap
        for (int i = 0; i<tree_cnt; i++) {
            int rank = curr.rank;
            HeapNode check_next = curr.next; // we want to be able to go back to the node that was supposed to be
                                            // next before we change everything and lose him
            if (buckets[rank] == null) { // if we don't have a tree of this rank yet
                buckets[rank] = curr;
            }
            else { // if we already have a tree of this rank, link them
                do {
                    root = this.link(curr, buckets[rank]);
                    buckets[rank] = null;
                    rank++;
                    curr = root;
                } while (buckets[rank] != null); // keep linking as long as the rank of the new tree matches a previous tree
                buckets[rank] = root;
            }
            curr = check_next;
        }
        this.tree_cnt = 0;
        int min = Integer.MAX_VALUE;
        boolean updated_first = false;
        HeapNode prev_node = null;
        for (int i= 0; i < buckets.length; i++) {
            curr = buckets[i];
            if (curr != null) {
                this.tree_cnt++;
                if (!updated_first) {
                    this.first = curr;
                    updated_first = true;
                }
                this.last = curr;
                if (curr.getKey() <= min) {
                    this.min = curr;
                    min = curr.getKey();
                }
                if (prev_node != null){ // takes care of the first node so it doesnt find its' prev
                    curr.prev = prev_node;
                    prev_node.next = curr;
                }
                prev_node = curr;
//                if (i > 0) {
//                    curr.prev = buckets[i-1];
//                }
//                if (i < buckets.length - 1) {
//                    curr.next = buckets[i+1];
//                }
            }
        }
        this.first.prev = this.last;
        this.last.next = this.first;

    }


    private void updateMaxRank() {
        HeapNode curr = first;
        int max = 0;
        for (int i = 0; i<tree_cnt; i++) {
            if (curr.rank > max) {
                max = curr.rank;
            }
            curr = curr.next;
        }
        max_rank = max;
    }

    private void cut(HeapNode node){
        HeapNode parent = node.parent;
        if (node.mark) {
            node.mark = false;
            marked_cnt--;
        }
        node.parent = null;
        parent.rank--;
        if (node.next == node){
            parent.child = null;
        }
        else{
            if (parent.child == node) {
                parent.child = node.next;
            }
            node.prev.next = node.next;
            node.next.prev = node.prev;
            node.prev = node;
            node.next = node;
        }
        cuts_cnt++;
        this.nodeInsert(node);
    }

    private HeapNode link(HeapNode x, HeapNode y){
        if (x.getKey() > y.getKey()) {
            HeapNode tmp = x;
            x = y;
            y = tmp;
        }
        y.prev.next = y.next;
        y.next.prev = y.prev;
        if (x.child != null) {
            y.next = x.child;
            y.prev = x.child.prev;
            x.child.prev.next = y;
            x.child.prev = y;
        }
        else{
            y.next = y;
            y.prev = y;
        }
        y.parent = x;
        x.child = y;
        x.rank++;
        link_cnt++;
        return x;
    }





    /**
     * public static int totalLinks()
     * <p>
     * This static function returns the total number of link operations made during the
     * run-time of the program. A link operation is the operation which gets as input two
     * trees of the same rank, and generates a tree of rank bigger by one, by hanging the
     * tree which has larger value in its root under the other tree.
     */
    public static int totalLinks() {
        return link_cnt; // should be replaced by student code
    }

    /**
     * public static int totalCuts()
     * <p>
     * This static function returns the total number of cut operations made during the
     * run-time of the program. A cut operation is the operation which disconnects a subtree
     * from its parent (during decreaseKey/delete methods).
     */
    public static int totalCuts() {
        return cuts_cnt; // should be replaced by student code
    }

    /**
     * public static int[] kMin(FibonacciHeap H, int k)
     * <p>
     * This static function returns the k smallest elements in a Fibonacci heap that contains a single tree.
     * The function should run in O(k*deg(H)). (deg(H) is the degree of the only tree in H.)
     * <p>
     * ###CRITICAL### : you are NOT allowed to change H.
     */

    public static int[] kMin(FibonacciHeap H, int k){
        int[] k_min = new int[k];
        if ( k == 0 ){return k_min;}
        if ( H.isEmpty() ){return k_min;}
        FibonacciHeap min_heap = new FibonacciHeap();
        HeapNode og_min = H.findMin();
        min_heap.insert(og_min.getKey());
        min_heap.first.corresponding_kmin_node = og_min;
        HeapNode og_node = og_min;
        for (int i = 0; i < k; i++){
            k_min[i] = min_heap.findMin().getKey();
            if (!min_heap.isEmpty()) {
                og_node = min_heap.findMin().corresponding_kmin_node;
            }
            min_heap.deleteMin();
            if (og_node.getChild() != null){
                HeapNode curr_child = og_node.getChild();
                do{
                    min_heap.insert(curr_child.getKey());
                    min_heap.first.corresponding_kmin_node = curr_child;
                    curr_child = curr_child.getNext();
                } while (curr_child != og_node.getChild());
            }
        }
        return k_min;
    }


//    public static int[] kMin(FibonacciHeap H, int k) {
//        int[] k_min = new int[k];
//        HeapNode[] nodes_arr = new HeapNode[H.size()];
//        int nodes_idx = 1;
//        HeapNode curr_min = H.findMin();
//        nodes_arr[0] = curr_min;
//        FibonacciHeap min_heap = new FibonacciHeap();
//        min_heap.insert(curr_min.getKey());
//        for(int i = 0; i < k; i++){
//            k_min[i] = min_heap.findMin().getKey();
//            min_heap.deleteMin();
//            if (min_heap.findMin() != null) {
//                int min_val = min_heap.findMin().getKey();
//
//                for (HeapNode node : nodes_arr) {
//                    if (node == null) {
//                        break;
//                    }
//                    if (node.getKey() == min_val) {
//                        curr_min = node;
//                        break;
//                    }
//                }
//            }
//            if (curr_min.child != null){
//                HeapNode smallest_child = curr_min.child;
//                HeapNode curr_child = curr_min.child;
//                HeapNode first_child = curr_child;
//                do{
//                    min_heap.insert(curr_child.getKey());
//                    nodes_arr[nodes_idx] = curr_child;
//                    curr_child = curr_child.next;
//                    nodes_idx++;
//                    if (curr_child.getKey() <= smallest_child.getKey()){
//                        smallest_child = curr_child;
//                    }
//                }while(curr_child != first_child);
//                curr_min = smallest_child;
//            }
//
//        }
//
//        return k_min;
//    }

    /**
     * public class HeapNode
     * <p>
     * If you wish to implement classes other than FibonacciHeap
     * (for example HeapNode), do it in this file, not in another file.
     */
    public static class HeapNode {

        public int key;
        private int rank;
        private boolean mark;
        private HeapNode child;
        private HeapNode next;
        private HeapNode prev;
        private HeapNode parent;

        private HeapNode corresponding_kmin_node;

        public HeapNode(int key) {
            this.key = key;
            this.next = this;
            this.prev = this;

        }

        public boolean getMarked() {
            return this.mark;
        }
        public int getKey() {
            return this.key;
        }
        public void setNext(HeapNode node) {
            this.next = node;
        }
        public void setPrev(HeapNode node){
            this.prev = node;
        }
        public int getRank(){return this.rank;}
        public HeapNode getChild(){return this.child;}
        public HeapNode getParent(){return this.parent;}
        public HeapNode getNext(){return this.next;}
        public HeapNode getPrev(){return this.prev;}
    }



}

