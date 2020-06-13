package ThreadExamples.ThreadLocal;

public class ThreadLocalExample {

    //private final ThreadLocal<Integer> counter = ThreadLocal.withInitial(() -> 0);
    private final ThreadLocal<Integer> counter = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };

    public static void main(String[] args) {
        ThreadLocalExample main = new ThreadLocalExample();
        main.start();
    }

    void start() {
        Thread worker1 = new Worker();
        Thread worker2 = new Worker();

        worker1.start();
        worker2.start();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // ignore
        }

        worker1.interrupt();
        worker2.interrupt();

        try {
            worker1.join();
            worker2.join();
        } catch (InterruptedException e) {
            // ignore
        }
    }

    class Worker extends Thread {

        @Override
        public void run() {
            while (true) {
                int currentValue = counter.get();
                System.out.println("The current value of the counter is " + currentValue
                        + " the thread id is " + Thread.currentThread().getId());
                counter.set(currentValue + 1);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return;
                }

                if (Thread.currentThread().isInterrupted())
                    return;
            }
        }
    }
}
