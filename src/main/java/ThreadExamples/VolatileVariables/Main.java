package ThreadExamples.VolatileVariables;

import java.util.HashSet;
import java.util.Set;

public class Main {

    private volatile int sharedVariable = 0;
    private volatile boolean isStopped = false;
    private volatile Set<Integer> cache = new HashSet<>();

    public static void main(String[] args) throws InterruptedException {
        Main main = new Main();

        Thread writerThread = new Thread(() -> {
            while (!main.isStopped) {
                main.sharedVariable += 1;

                Set<Integer> tempCache = main.cache;
                System.out.println("The content of the cache is: " + tempCache + " thread id is: " + Thread.currentThread().getId());

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        });

        Thread readerThread = new Thread(() -> {
            while (!main.isStopped) {
                System.out.println("Shared variable is: " + main.sharedVariable);

                Set<Integer> tempCache = main.cache;
                System.out.println("The content of the cache is: " + tempCache + " thread id is: " + Thread.currentThread().getId());

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        });

        writerThread.start();
        readerThread.start();

        Set<Integer> newCache = new HashSet<>();
        newCache.add(1);
        newCache.add(2);
        newCache.add(3);

        main.cache = newCache;

        Thread.sleep(2000);

        Set<Integer> newCache2 = new HashSet<>();
        newCache2.add(4);
        newCache2.add(5);
        newCache2.add(6);

        main.cache = newCache2;

        Thread.sleep(4000);

        main.isStopped = true;

        writerThread.join();
        readerThread.join();
    }
}
