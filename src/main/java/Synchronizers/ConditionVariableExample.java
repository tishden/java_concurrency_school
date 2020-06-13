package Synchronizers;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionVariableExample {
    private final Queue<WorkItem> workQueue = new LinkedList<>();
    private final int queueSize = 100;
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    public static void main(String[] args) {
        ConditionVariableExample main = new ConditionVariableExample();
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

            Thread.sleep(500);

            System.out.println("The work item " + name + " is processed in the thread " + Thread.currentThread().getId());
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
