package AdvancedAlgorithms;

import java.util.concurrent.locks.ReentrantLock;

public class Spinlock {
    //private final ReentrantLock lock = new ReentrantLock(true);
    private volatile boolean ping = true;

    public static void main(String[] args) throws InterruptedException {
        Spinlock main = new Spinlock();
        main.start();
    }

    void start() throws InterruptedException {
        Ping ping = new Ping();
        Pong pong = new Pong();

        ping.start();
        pong.start();

        Thread.sleep(5000);

        ping.interrupt();
        pong.interrupt();

        ping.join();
        pong.join();
    }

    class Ping extends Thread {
        @Override
        public void run() {
            while (!isInterrupted()) {
                //if (lock.tryLock())
                try {
                    if (ping == false)
                        continue;
                    System.out.println("Ping");
                    ping = false;
                } finally {
                //    lock.unlock();
                }
            }
        }
    }

    class Pong extends Thread {
        @Override
        public void run() {
            while (!isInterrupted()) {
                //lock.lock();
                try {
                    if (ping == true)
                        continue;
                    System.out.println("Pong");
                    ping = true;
                } finally {
                //    lock.unlock();
                }
            }
        }
    }
}
