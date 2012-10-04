package Engine;

public class QuickSort {

    // A quick sort for named elements.

    public static void sort(int a[], int lo0, int hi0, boolean casep, Machine machine) {

        int lo = lo0;
        int hi = hi0;
        int mid;

        if (hi0 > lo0) {

            // Arbitrarily establishing partition element as the midpoint of the array

            mid = a[(lo0 + hi0) / 2];
            
            // loop through the array until indices cross
            while (lo <= hi) {
                /*
                 * find the first element that is greater than or equal to the partition element starting from the left Index.
                 */
                while ((lo < hi0) && (less(a[lo], mid, casep, machine)))
                    ++lo;
                
                /*
                 * find an element that is smaller than or equal to the partition element starting from the right Index.
                 */
                while ((hi > lo0) && (greater(a[hi], mid, casep, machine)))
                    --hi;
                
                // if the indexes have not crossed, swap
                if (lo <= hi) {
                    swap(a, lo, hi);
                    ++lo;
                    --hi;
                }
            }

            /*
             * If the right index has not reached the left side of array must now sort the left partition.
             */
            if (lo0 < hi) sort(a, lo0, hi, casep, machine);

            /*
             * If the left index has not reached the right side of array must now sort the right partition.
             */
            if (lo < hi0) sort(a, lo, hi0, casep, machine);

        }
    }

    private static boolean less(int o1, int o2, boolean casep, Machine machine) {
        int n1 = machine.getName(o1);
        int n2 = machine.getName(o2);
        if (n1 != -1 && n2 != -1) {
            String s1 = machine.valueToString(n1);
            String s2 = machine.valueToString(n2);
            if(casep)
                return s1.compareTo(s2) < 0;
            else return s1.compareToIgnoreCase(s2) < 0;
        } else
            return true;
    }

    private static boolean greater(int o1, int o2, boolean casep, Machine machine) {
        int n1 = machine.getName(o1);
        int n2 = machine.getName(o2);
        if (n1 != -1 && n2 != -1) {
            String s1 = machine.valueToString(n1);
            String s2 = machine.valueToString(n2);
            if(casep)
                return s1.compareTo(s2) > 0;
            else return s1.compareToIgnoreCase(s2) > 0;
        } else
            return false;
    }

    private static void swap(int a[], int i, int j) {
        int T;
        T = a[i];
        a[i] = a[j];
        a[j] = T;

    }

    public static void sort(int a[], boolean casep, Machine machine) {
        sort(a, 0, a.length - 1, casep, machine);
    }
}
