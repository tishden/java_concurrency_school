package CollectionsExamples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class CopyOnWrite {
    // Version 1 with synchronized
    //private List<Integer> array = new ArrayList<>();
    //private Object lock = new Object();

    // Version 2 with Collections.synchronizedList: an iterator is not thread safe in the reader thread
    //private List<Integer> array = Collections.synchronizedList(new ArrayList<>());

    // Version 3 CopyOnWriteArrayList
    private CopyOnWriteArrayList<Integer> array = new CopyOnWriteArrayList<>();
    //CopyOnWriteArraySet<>

    public static void main(String[] args) throws InterruptedException {
        CopyOnWrite main = new CopyOnWrite();
        main.start();
    }

    void start() throws InterruptedException {
        Writer writer = new Writer();
        Reader reader = new Reader();

        writer.start();
        reader.start();

        writer.join();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            return;
        }
        reader.interrupt();
    }

    class Writer extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return;
            }
            //synchronized (lock) {
                for (int i = 0; i < 10_000; i++) {
                    array.add(i);
                }
            //}
            System.out.println("The array is filled");
        }
    }

    class Reader extends Thread {
        @Override
        public void run() {
            while (true) {
                int counter = 0;
                //synchronized (lock) {
                    Iterator<Integer> it = array.iterator();
                    while (it.hasNext()) {
                        it.next();
                        // it.remove(); // unsupported for CopyOnWriteArrayList
                        counter++;
                    }
                //}
                System.out.println("The size of the array is " + counter);

                try {
                    Thread.sleep(0, 1000);
                } catch (InterruptedException e) {
                    return;
                }

                if (isInterrupted())
                    return;
            }
        }
    }
}
