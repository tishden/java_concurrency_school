package ConcurrentBugs;

public class RaceCondition {

    private volatile boolean haveNewData = true;
    private final Object lock = new Object();

    public static void main(String[] args) throws InterruptedException {
        DataRace main = new DataRace();
        main.start();
    }

    void start() throws InterruptedException {
        MyThread thread1 = new MyThread();
        MyThread thread2 = new MyThread();

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();
    }

    class MyThread extends Thread {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                if (haveNewData) {
                    synchronized (lock) {
                        // if (haveNewData) { // without this check we have race-condition
                            // loading new data
                            haveNewData = false;
                        // }
                    }
                }
            }
        }
    }
}
