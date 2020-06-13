package Synchronizers;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteLockExample {

    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);
    private int counter = 0;

    public static void main(String[] args) throws InterruptedException {
        ReadWriteLockExample main = new ReadWriteLockExample();
        main.start();
    }

    void start() throws InterruptedException {
        WriterThread writer1 = new WriterThread();
        WriterThread writer2 = new WriterThread();

        ReaderThread reader1 = new ReaderThread();
        ReaderThread reader2 = new ReaderThread();

        writer1.start();
        writer2.start();
        reader1.start();
        reader2.start();

        Thread.sleep(5000);

        writer1.interrupt();
        writer2.interrupt();
        reader1.interrupt();
        reader2.interrupt();

        writer1.join();
        writer2.join();
        reader1.join();
        reader2.join();
    }

    class ReaderThread extends Thread {

        @Override
        public void run() {
            while (true) {
                lock.readLock().lock();
                try {
                    System.out.println("The read lock is acquired");

                    System.out.println("The counter is: " + counter);

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    System.out.println("The read lock is released");
                } finally {
                    lock.readLock().unlock();
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
                lock.writeLock().lock();
                try {
                    System.out.println("The write lock is acquired");

                    counter++;

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    System.out.println("The write lock is released");
                } finally {
                    lock.readLock().lock(); // downgrading the write lock to the reader
                    System.out.println("The read lock is acquired in the writer thread");
                    lock.writeLock().unlock();

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    System.out.println("The read lock is released in the writer thread");
                    lock.readLock().unlock();
                }

                if (Thread.currentThread().isInterrupted())
                    return;
            }
        }
    }
}
