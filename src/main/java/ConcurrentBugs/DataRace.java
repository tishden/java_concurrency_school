package ConcurrentBugs;

public class DataRace {

    private final Object lock = new Object();
    private int counter; // shared variable

    public static void main(String[] args) throws InterruptedException {
        DataRace main = new DataRace();
        main.start();
    }

    void start() throws InterruptedException {
        ThreadWithDataRace thread1 = new ThreadWithDataRace();
        ThreadWithDataRace thread2 = new ThreadWithDataRace();

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        System.out.println("The counter value is: " + counter);
    }

    class ThreadWithDataRace extends Thread {
        @Override
        public void run() {
            for (int i = 0; i < 1_000_000; i++) {
                //synchronized (lock) {
                    counter++; // data-race
                //}
            }
        }
    }
}
