package byow.proj2ab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

public class ArrayHeapMinPQ<T> implements ExtrinsicMinPQ<T> {
    private List<Node> heap;
    private HashMap<T, Integer> indices;

    public ArrayHeapMinPQ() {
        heap = new ArrayList<>();
        indices = new HashMap<>();
    }

    @Override
    public void add(T item, double priority) {
        if (contains(item)) {
            throw new IllegalArgumentException("Item already exists in heap");
        }
        heap.add(new Node(item, priority));

        int index = heap.size() - 1;
        indices.put(item, index);
        swapUpwards(index);
    }

    @Override
    public boolean contains(T item) {
        return indices.containsKey(item);
    }

    @Override
    public T getSmallest() {
        if (heap.size() == 0) {
            throw new NoSuchElementException("No elements in heap");
        }
        return heap.get(0).item;
    }

    @Override
    public T removeSmallest() {
        if (heap.size() == 0) {
            throw new NoSuchElementException("No elements in heap");
        }
        T smallest = heap.get(0).item;
        indices.remove(smallest);
        if (heap.size() == 1) {
            return heap.remove(0).item;
        }
        heap.set(0, heap.remove(heap.size() - 1));
        indices.put(heap.get(0).item, 0);
        swapDownwards(0);
        return smallest;
    }

    @Override
    public int size() {
        return heap.size();
    }

    @Override
    public void changePriority(T item, double priority) {
        if (!contains(item)) {
            throw new NoSuchElementException("Item not in heap");
        }
        int index = indices.get(item);
        double oldPriority = heap.get(index).priority;
        heap.get(index).priority = priority;
        if (priority < oldPriority) {
            swapUpwards(index);
        } else {
            swapDownwards(index);
        }
    }

    private int getLeft(int index) {
        return 2 * index + 1;
    }

    private int getRight(int index) {
        return 2 * index + 2;
    }

    private int getParent(int index) {
        return (index - 1) / 2;
    }

    private void swap(int a, int b) {
        Node temp = heap.get(a);
        heap.set(a, heap.get(b));
        heap.set(b, temp);
        indices.put(heap.get(a).item, a);
        indices.put(heap.get(b).item, b);
    }

    private void swapUpwards(int index) {
        int parent = getParent(index);
        while (index > 0 && heap.get(index).compareTo(heap.get(parent)) < 0) {
            swap(index, parent);
            index = parent;
            parent = getParent(index);
        }
    }

    private void swapDownwards(int index) {
        int left = getLeft(index);
        int right = getRight(index);
        boolean swapLeft = left < heap.size() && heap.get(index).compareTo(heap.get(left)) > 0;
        boolean swapRight = right < heap.size() && heap.get(index).compareTo(heap.get(right)) > 0;
        while (swapLeft || swapRight) {
            if (swapLeft && swapRight) {
                if (heap.get(left).compareTo(heap.get(right)) <= 0) {
                    swap(index, left);
                    index = left;
                } else {
                    swap(index, right);
                    index = right;
                }
            } else if (swapLeft) {
                swap(index, left);
                index = left;
            } else if (swapRight) {
                swap(index, right);
                index = right;
            }
            left = getLeft(index);
            right = getRight(index);
            swapLeft = left < heap.size() && heap.get(index).compareTo(heap.get(left)) > 0;
            swapRight = right < heap.size() && heap.get(index).compareTo(heap.get(right)) > 0;
        }
    }

    private class Node implements Comparable<Node> {
        private T item;
        private double priority;

        Node(T item, double priority) {
            this.item = item;
            this.priority = priority;
        }

        @Override
        public int compareTo(Node o) {
            if (priority < o.priority) {
                return -1;
            } else if (priority > o.priority) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
