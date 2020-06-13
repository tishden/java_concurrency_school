package PerformanceAndTesting;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class ProducerConsumerPerformance {
    private final Queue<WorkItem> workQueue = new LinkedList<>();
    private final int queueSize = 100;
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private final AtomicLong counter = new AtomicLong(0);
    private final AtomicReference<ConcurrentLinkedQueue<Long>> latencies = new AtomicReference<>(new ConcurrentLinkedQueue<>());

    public static void main(String[] args) {
        ProducerConsumerPerformance main = new ProducerConsumerPerformance();
        main.start();
    }

    void start() {
        Thread producer1 = new Producer();
        Thread producer2 = new Producer();

        Thread consumer1 = new Consumer();
        Thread consumer2 = new Consumer();
        Thread consumer3 = new Consumer();

        producer1.start();
        producer2.start();
        consumer1.start();
        consumer2.start();
        consumer3.start();

        List<Long> throughputs = new ArrayList<>();
        List<Long> percentiles = new ArrayList<>();
        double percentile = 95;
        for (int i = 0; i < 5; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignore
            }
            throughputs.add(counter.getAndSet(0));
            ConcurrentLinkedQueue<Long> currentLatencies = latencies.getAndSet(new ConcurrentLinkedQueue<>());
            List<Long> sortedLatencies = currentLatencies.stream().sorted().collect(Collectors.toList());
            Long currentPercentile = sortedLatencies.get((int) (sortedLatencies.size() * percentile / 100.0));
            percentiles.add(currentPercentile);
        }

        producer1.interrupt();
        producer2.interrupt();
        consumer1.interrupt();
        consumer2.interrupt();
        consumer3.interrupt();

        try {
            producer1.join();
            producer2.join();
            consumer1.join();
            consumer2.join();
            consumer3.join();
        } catch (InterruptedException e) {
            // ignore
        }

        System.out.println("Throughputs are " + throughputs + " items per second");
        System.out.println(percentile + " % of requests were processed at " + percentiles + " nanoseconds");
    }

    class WorkItem {
        private final long startTime;
        private long endTime = 0;
        private final String name;
        WorkItem(String name) {
            this.name = name;
            this.startTime = System.nanoTime();
        }

        void process() throws InterruptedException {

            System.out.println("Starting to process a work item " + name);

            //Thread.sleep(500);

            System.out.println("The work item " + name + " is processed in the thread " + Thread.currentThread().getId());
            counter.incrementAndGet();
            endTime = System.nanoTime();
            latencies.get().add(getLatency());
        }

        long getLatency() {
            return endTime - startTime;
        }
    }


    class Producer extends Thread {

        @Override
        public void run() {
            int name = 0;
            while (true) {
                lock.lock();
                try {
                    if (workQueue.size() < queueSize) {
                        WorkItem workItem = new WorkItem(String.valueOf(name) + "-" + Thread.currentThread().getId());
                        workQueue.add(workItem);
                        condition.signalAll();
                        System.out.println("Populated a work item " + name);
                        name++;
                    } else {
                        System.out.println("The queue if full trying again...");

                        try {
                            condition.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                } finally {
                    lock.unlock();
                }

                if (Thread.currentThread().isInterrupted())
                    return;
            }
        }
    }

    class Consumer extends Thread {

        @Override
        public void run() {
            while (true) {
                WorkItem workItem;
                lock.lock();
                try {
                    while (true) {
                        workItem = workQueue.poll();
                        condition.signalAll();
                        //condition.notifyAll(); // don't do that for the condition variable
                        if (workItem == null) {
                            try {
                                condition.await();
                                //condition.wait(); // don't do that for the condition variable
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                        } else {
                            break;
                        }
                    }
                } finally {
                    lock.unlock();
                }

                try {
                    workItem.process();
                } catch (InterruptedException e) {
                    return;
                }

                if (Thread.currentThread().isInterrupted())
                    return;
            }
        }
    }
}
