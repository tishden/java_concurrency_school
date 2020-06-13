package Synchronizers;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockExample {

    private final Lock lock1 = new ReentrantLock();
    private int counter1;

    private final Lock lock2 = new ReentrantLock(true);
    private int counter2;

    public static void main(String[] args) throws InterruptedException {
        ReentrantLockExample main = new ReentrantLockExample();
        main.start();
    }

    void start() throws InterruptedException {
        WriterThread writer = new WriterThread();
        ReaderThread reader = new ReaderThread();

        writer.start();
        reader.start();

        Thread.sleep(5000);

        writer.interrupt();
        reader.interrupt();

        writer.join();
        reader.join();
    }

    class ReaderThread extends Thread {

        @Override
        public void run() {
            while (true) {
                /*
                    synchronized (lock1) { // !!! don't do this !!!

                    }
                */

                lock1.lock();
                try {
                    System.out.println("The counter1 is: " + counter1);
                } finally {
                    try {
                    lock2.lockInterruptibly();
                        // can do something with counter1 and counter2
                        System.out.println("The counter1 + counter2 is: " + counter1 + counter2);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        lock1.unlock();
                        try {
                            if (!Thread.currentThread().isInterrupted()) {
                                System.out.println("The counter2 is: " + counter2);
                            }
                        } finally {
                            lock2 .unlock();
                        }
                    }
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                if (Thread.currentThread().isInterrupted())
                    return;
            }
        }
    }

    class WriterThread extends Thread {

        @Override
        public void run() {
            while (true) {
                lock1.lock();
                try {
                    counter1++;
                } finally {
                    lock1.unlock();
                }

                try {
                    if (lock2.tryLock(2, TimeUnit.MILLISECONDS)) {
                        try {
                            counter2++;
                        } finally {
                            lock2.unlock();
                        }
                    } else {
                        System.out.println("The writer is unable to to get the lock2");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                if (Thread.currentThread().isInterrupted())
                    return;
            }
        }
    }
}
