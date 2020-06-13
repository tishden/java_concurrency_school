package AdvancedAlgorithms;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public class LockFreeQueue {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 1. There is guaranteed system-wide progress or at least one thread do progress
        // 2. Failure or suspension of any thread cannot cause failure or suspension of another thread
        // wait-free
        // 3. An operation will be completed in finite and bounded number of steps.

        LockFreeQueue main = new LockFreeQueue();
        main.start();
    }

    void start() throws ExecutionException, InterruptedException {
        Queue<Integer> queue = new Queue<>();

        ExecutorService executor = Executors.newFixedThreadPool(6);
        Future<Integer> conResult1 = executor.submit(new Consumer(queue));
        Future<Integer> conResult2 = executor.submit(new Consumer(queue));
        Future<Integer> conResult3 = executor.submit(new Consumer(queue));

        Future<Integer> pubResult1 = executor.submit(new Publisher(queue));
        Future<Integer> pubResult2 = executor.submit(new Publisher(queue));
        Future<Integer> pubResult3 = executor.submit(new Publisher(queue));

        Integer publishersSum = pubResult1.get() + pubResult2.get() + pubResult3.get();
        Thread.sleep(2000);
        executor.shutdownNow();
        Integer consumersSum = conResult1.get() + conResult2.get() + conResult3.get();
        System.out.println(publishersSum);
        System.out.println(consumersSum);
    }

    class Publisher implements Callable<Integer> {
        private final Queue<Integer> queue;

        Publisher(Queue<Integer> queue) {
            this.queue = queue;
        }

        public Integer call() {
            int sum = 0;
            for (int i = 0; i < 1000; i++) {
                queue.put(i);
                    sum += i;
            }
            return sum;
        }
    }

    class Consumer implements Callable<Integer> {
        private final Queue<Integer> queue;

        Consumer(Queue<Integer> queue) {
            this.queue = queue;
        }

        public Integer call() {
            int sum = 0;
            while(!Thread.currentThread().isInterrupted()) {
                Integer value = queue.take();
                if (value != null)
                    sum += value;
            }
            return sum;
        }
    }
    static class Queue<T> {
        //private final ReentrantLock lock = new ReentrantLock();
        private final AtomicReference<Node<T>> head = new AtomicReference<>();

        public void put(T value) {
            //lock.lock();
            //try {
            while (true) {
                Node<T> currentHead = head.get();
                Node<T> newNode = new Node<T>(value, currentHead);
                if (head.compareAndSet(currentHead, newNode)) // lock-free style
                    return;
            }
            //} finally {
            //    lock.unlock();
            //}
        }

        public T take() {
            //lock.lock();
            //try {
            while (true) {
                Node<T> result = head.get();
                if (result == null)
                    return null;

                Node<T> prev = result.prev;
                if (head.compareAndSet(result, prev)) // lock-free style
                    return result.data;
            }
            //} finally {
            //    lock.unlock();
            //}
        }
    }

    static class Node<T> {
        private final T data;
        private final Node<T> prev;

        Node(T data, Node prev) {
            this.data = data;
            this.prev = prev;
        }
    }
}
