package CollectionsExamples;

import java.util.*;

public class SynchronizedCollections {

    public static void main(String[] args) {
        // Old style classes
        Vector<Integer> v = new Vector<>();
        v.add(1);

        Hashtable<Integer, Integer> ht = new Hashtable<>();
        ht.put(1, 2);

        StringBuffer sb = new StringBuffer();
        sb.append("1");
        sb.append("2");

        // New style
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append("1");
        sBuilder.append("2");

        List<Integer> list = new ArrayList<>();
        Collection<Integer> threadSafeList = java.util.Collections.synchronizedCollection(list);
        //java.util.SynchronizedCollections.synchronizedList()
        //java.util.SynchronizedCollections.synchronizedSet()
        //java.util.SynchronizedCollections.synchronizedMap()
    }
}
