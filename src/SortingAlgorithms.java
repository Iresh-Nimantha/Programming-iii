public class SortingAlgorithms {

    private SortingAlgorithms() { }

    public static void insertionSort(Entry[] a) {
        for (int i = 1; i < a.length; i++) {
            Entry current = a[i];
            int j = i - 1;
            while (j >= 0 && a[j].key > current.key) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = current;
        }
    }

    public static void shellSort(Entry[] a) {
        int n = a.length;
        int gap = 1;
        while (gap < n / 3) {
            gap = 3 * gap + 1;
        }
        while (gap >= 1) {
            for (int i = gap; i < n; i++) {
                Entry current = a[i];
                int j = i;
                while (j >= gap && a[j - gap].key > current.key) {
                    a[j] = a[j - gap];
                    j -= gap;
                }
                a[j] = current;
            }
            gap /= 3;
        }
    }

    public static void mergeSort(Entry[] a) {
        if (a.length < 2) return;
        Entry[] aux = new Entry[a.length];
        mergeSort(a, aux, 0, a.length - 1);
    }

    private static void mergeSort(Entry[] a, Entry[] aux, int lo, int hi) {
        if (lo >= hi) return;
        int mid = lo + (hi - lo) / 2;
        mergeSort(a, aux, lo, mid);
        mergeSort(a, aux, mid + 1, hi);
        if (a[mid].key <= a[mid + 1].key) return;
        merge(a, aux, lo, mid, hi);
    }

    private static void merge(Entry[] a, Entry[] aux, int lo, int mid, int hi) {
        System.arraycopy(a, lo, aux, lo, hi - lo + 1);
        int i = lo, j = mid + 1;
        for (int k = lo; k <= hi; k++) {
            if (i > mid)                        a[k] = aux[j++];
            else if (j > hi)                    a[k] = aux[i++];
            else if (aux[j].key < aux[i].key)   a[k] = aux[j++];
            else                                a[k] = aux[i++];
        }
    }

    public static void quickSort(Entry[] a) {
        if (a.length < 2) return;
        quickSort(a, 0, a.length - 1);
    }

    private static void quickSort(Entry[] a, int lo, int hi) {
        while (lo < hi) {
            double pivot = medianOfThree(a[lo].key, a[lo + (hi - lo) / 2].key, a[hi].key);
            int i = lo, j = hi;
            while (i <= j) {
                while (a[i].key < pivot) i++;
                while (a[j].key > pivot) j--;
                if (i <= j) {
                    swap(a, i, j);
                    i++;
                    j--;
                }
            }
            if (j - lo < hi - i) {
                quickSort(a, lo, j);
                lo = i;
            } else {
                quickSort(a, i, hi);
                hi = j;
            }
        }
    }

    private static double medianOfThree(double x, double y, double z) {
        if ((x <= y && y <= z) || (z <= y && y <= x)) return y;
        if ((y <= x && x <= z) || (z <= x && x <= y)) return x;
        return z;
    }

    public static void heapSort(Entry[] a) {
        int n = a.length;
        for (int i = n / 2 - 1; i >= 0; i--) {
            siftDown(a, i, n);
        }
        for (int end = n - 1; end > 0; end--) {
            swap(a, 0, end);
            siftDown(a, 0, end);
        }
    }

    private static void siftDown(Entry[] a, int i, int size) {
        while (true) {
            int left = 2 * i + 1;
            int right = left + 1;
            int largest = i;
            if (left < size && a[left].key > a[largest].key)   largest = left;
            if (right < size && a[right].key > a[largest].key) largest = right;
            if (largest == i) return;
            swap(a, i, largest);
            i = largest;
        }
    }

    private static void swap(Entry[] a, int i, int j) {
        Entry t = a[i];
        a[i] = a[j];
        a[j] = t;
    }

    public static boolean isSorted(Entry[] a) {
        for (int i = 1; i < a.length; i++) {
            if (a[i - 1].key > a[i].key) return false;
        }
        return true;
    }
}
