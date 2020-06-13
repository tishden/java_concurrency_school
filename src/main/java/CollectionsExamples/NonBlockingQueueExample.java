package CollectionsExamples;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NonBlockingQueueExample {
    private final Queue<WorkItem> workQueue = new ConcurrentLinkedQueue<>();
    //ConcurrentLinkedDeque

    private final int queueSize = 100;

    public static void main(String[] args) {
        NonBlockingQueueExample main = new NonBlockingQueueExample();
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

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // ignore
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
    }

    class WorkItem {

        private final String name;

        WorkItem(String name) {
            this.name = name;
        }

        void process() throws InterruptedException {

            System.out.println("Starting to process a work item " + name);

            //Thread.sleep(500);

            System.out.println("The work item " + name + " is processed in the thread " + Thread.currentThread().getId());
        }
    }

    class Producer extends Thread {

        @Override
        public void run() {
            int name = 0;
            while (true) {
                if (workQueue.size() < queueSize) {
                    WorkItem workItem = new WorkItem(String.valueOf(name) + "-" + Thread.currentThread().getId());
                    workQueue.add(workItem);
                    System.out.println("Populated a work item " + name);
                    name++;
                } else {
                    System.out.println("The queue if full trying again...");
                    try {
                        Thread.sleep(0, 10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
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
                while (true) {
                    workItem = workQueue.poll();

                    if (workItem == null) {
                        try {
                            Thread.sleep(0, 10);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    } else {
                        break;
                    }
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
