package ConcurrentBugs;

import java.util.HashMap;
import java.util.Map;

public class DeadLock {
    private final Map<Integer, Integer> cacheA = new HashMap<>();
    private final Map<Integer, Integer> cacheB = new HashMap<>();

    private final Object cacheALock_1 = new Object();
    private final Object cacheBLock_2 = new Object();

    public static void main(String[] args) {
        DeadLock  main = new DeadLock();
        main.start();

    }

    void start() {
        Thread builder = new Thread(() -> {
            int counter = 0;
            while (true) {
                buildCache(counter, counter, counter);
                counter++;
            }
        });

        Thread searcher = new Thread(() ->{
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignore
            }

            int counter = 0;
            while (true) {
                int result = searchInCache(counter);
                System.out.println("Result is " + result);
                counter++;
            }
        });
        builder.start();
        searcher.start();

        try {
            Thread.sleep(60_000);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    void buildCache(Integer key, Integer valueA, Integer valueB) {
        synchronized (cacheALock_1) {
            synchronized (cacheBLock_2) {
                cacheA.put(key, valueA);
                cacheB.put(key, valueB);
            }
        }
    }

    Integer searchInCache(Integer key) {
        //synchronized (cacheALock_1) { // this is the fix for the dead-lock
        synchronized (cacheBLock_2) {
            synchronized (cacheALock_1) { // the reason of the dead-lock
                Integer valueA = cacheA.getOrDefault(key, 0);
                Integer valueB = cacheB.getOrDefault(key, 0);
                return valueA + valueB;
            }
        }
    }

    void transferMoney(Account accountA, Account accountB) {
        Object lock1 = accountA.id <= accountB.id ? accountA : accountB;
        Object lock2 = accountA.id > accountB.id ? accountA : accountB;

        synchronized (lock1) {
            synchronized (lock2) {
                // transfer money
            }
        }
    }

    class Account {
        int id;
    }
}
