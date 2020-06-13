package ConcurrentBugs;

public class Starvation {

    private final Object lock = new Object();
    private int counter; // shared variable

    public static void main(String[] args) throws InterruptedException {
        Starvation main = new Starvation();
        main.start();
    }

    void start() throws InterruptedException {
        MyThread thread1 = new MyThread();
        MyThread thread2 = new MyThread();

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        System.out.println("The counter value is: " + counter);
    }

    class MyThread extends Thread {
        @Override
        public void run() {
            for (int i = 0; i < 1_000_000; i++) {
                synchronized (lock) { // starvation, both threads interfere with each other
                    counter++;
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }
    }

    /* Fix for the starvation
    class MyThread extends Thread {
        @Override
        public void run() {
            int tempCounter = 0;
            for (int i = 0; i < 1_000_000; i++) {
                tempCounter++;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    return;
                }
            }

            synchronized (lock) {
                counter += tempCounter;
            }
        }
    }
    */
}
