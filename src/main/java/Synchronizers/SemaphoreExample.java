package Synchronizers;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class SemaphoreExample {

    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    private final Semaphore semaphore = new Semaphore(NUMBER_OF_CORES, true);

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Available processor cores is " + NUMBER_OF_CORES);

        SemaphoreExample main = new SemaphoreExample();
        main.start();
    }

    void start() throws InterruptedException {
        int numberOfWorkers = NUMBER_OF_CORES + 2;
        ArrayList<WorkerThread> workers = new ArrayList<>(numberOfWorkers);
        for (int i = 0; i < numberOfWorkers; i++) {
            WorkerThread worker = new WorkerThread();
            workers.add(worker);
        }

        for (WorkerThread worker : workers) {
            worker.start();
        }

        for (WorkerThread worker : workers) {
            worker.join();
        }


    }
    class WorkerThread extends Thread {

        @Override
        public void run() {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                return;
            }

            try {
                System.out.println("Doing useful work in the thread " + Thread.currentThread().getId());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("Work is done in the thread " + Thread.currentThread().getId());
            } finally {
                semaphore.release();
            }
        }
    }
}
