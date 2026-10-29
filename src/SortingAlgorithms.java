/**
 * In-place sorting algorithms for the numeric entries extracted from a dataset.
 *
 * <p>Every operation moves complete {@link Entry} references, so each value
 * remains associated with the row from which it was extracted.</p>
 */
public final class SortingAlgorithms {

    private SortingAlgorithms() {
        // Utility class.
    }

    /** Average and worst case: O(n^2). */
    public static void insertionSort(Entry[] entries) {
        requireEntries(entries);
        for (int i = 1; i < entries.length; i++) {
            Entry value = entries[i];
            int position = i - 1;
            while (position >= 0 && compare(entries[position], value) > 0) {
                entries[position + 1] = entries[position];
                position--;
            }
            entries[position + 1] = value;
        }
    }

    /**
     * Shell Sort using the halving gap sequence n/2, n/4, ..., 1.
     * Complexity depends on the gap sequence.
     */
    public static void shellSort(Entry[] entries) {
        requireEntries(entries);
        for (int gap = entries.length / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < entries.length; i++) {
                Entry value = entries[i];
                int position = i;
                while (position >= gap
                        && compare(entries[position - gap], value) > 0) {
                    entries[position] = entries[position - gap];
                    position -= gap;
                }
                entries[position] = value;
            }
        }
    }

    /** Average and worst case: O(n log n). */
    public static void mergeSort(Entry[] entries) {
        requireEntries(entries);
        if (entries.length < 2) {
            return;
        }
        Entry[] workspace = new Entry[entries.length];
        mergeSort(entries, workspace, 0, entries.length - 1);
    }

    private static void mergeSort(Entry[] entries, Entry[] workspace,
                                  int low, int high) {
        if (low >= high) {
            return;
        }
        int middle = low + (high - low) / 2;
        mergeSort(entries, workspace, low, middle);
        mergeSort(entries, workspace, middle + 1, high);
        if (compare(entries[middle], entries[middle + 1]) <= 0) {
            return;
        }
        merge(entries, workspace, low, middle, high);
    }

    private static void merge(Entry[] entries, Entry[] workspace,
                              int low, int middle, int high) {
        for (int i = low; i <= high; i++) {
            workspace[i] = entries[i];
        }
        int left = low;
        int right = middle + 1;
        for (int target = low; target <= high; target++) {
            if (left > middle) {
                entries[target] = workspace[right++];
            } else if (right > high) {
                entries[target] = workspace[left++];
            } else if (compare(workspace[left], workspace[right]) <= 0) {
                entries[target] = workspace[left++];
            } else {
                entries[target] = workspace[right++];
            }
        }
    }

    /**
     * Quick Sort with median-of-three pivot selection and a three-way
     * partition. The smaller partition is processed first, limiting stack
     * depth even when the input is already ordered. Average: O(n log n);
     * worst case: O(n^2).
     */
    public static void quickSort(Entry[] entries) {
        requireEntries(entries);
        quickSort(entries, 0, entries.length - 1);
    }

    private static void quickSort(Entry[] entries, int low, int high) {
        while (low < high) {
            Entry pivot = median(entries[low], entries[low + (high - low) / 2],
                    entries[high]);
            int less = low;
            int current = low;
            int greater = high;
            while (current <= greater) {
                int comparison = compare(entries[current], pivot);
                if (comparison < 0) {
                    swap(entries, less++, current++);
                } else if (comparison > 0) {
                    swap(entries, current, greater--);
                } else {
                    current++;
                }
            }
            if (less - low < high - greater) {
                quickSort(entries, low, less - 1);
                low = greater + 1;
            } else {
                quickSort(entries, greater + 1, high);
                high = less - 1;
            }
        }
    }

    /** Average and worst case: O(n log n). */
    public static void heapSort(Entry[] entries) {
        requireEntries(entries);
        for (int parent = entries.length / 2 - 1; parent >= 0; parent--) {
            siftDown(entries, parent, entries.length);
        }
        for (int end = entries.length - 1; end > 0; end--) {
            swap(entries, 0, end);
            siftDown(entries, 0, end);
        }
    }

    private static void siftDown(Entry[] entries, int root, int size) {
        while (root < size / 2) {
            int child = root * 2 + 1;
            if (child + 1 < size && compare(entries[child], entries[child + 1]) < 0) {
                child++;
            }
            if (compare(entries[root], entries[child]) >= 0) {
                return;
            }
            swap(entries, root, child);
            root = child;
        }
    }

    /** Returns true when entries are in ascending numerical order. */
    public static boolean isSorted(Entry[] entries) {
        requireEntries(entries);
        for (int i = 1; i < entries.length; i++) {
            if (compare(entries[i - 1], entries[i]) > 0) {
                return false;
            }
        }
        return true;
    }

    private static int compare(Entry first, Entry second) {
        return Double.compare(first.key, second.key);
    }

    private static Entry median(Entry first, Entry second, Entry third) {
        if (compare(first, second) < 0) {
            if (compare(second, third) < 0) return second;
            return compare(first, third) < 0 ? third : first;
        }
        if (compare(first, third) < 0) return first;
        return compare(second, third) < 0 ? third : second;
    }

    private static void swap(Entry[] entries, int first, int second) {
        Entry temporary = entries[first];
        entries[first] = entries[second];
        entries[second] = temporary;
    }

    private static void requireEntries(Entry[] entries) {
        if (entries == null) {
            throw new IllegalArgumentException("Entries must not be null.");
        }
        for (Entry entry : entries) {
            if (entry == null) {
                throw new IllegalArgumentException("Entries must not contain null values.");
            }
        }
    }
}
