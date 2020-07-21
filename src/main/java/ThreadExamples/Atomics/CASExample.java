package ThreadExamples.Atomics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CASExample {
    private static final int NUMBER_OF_ITERATIONS = 10_000_000;
    private static final int NUMBER_OF_THREADS = 2;

    private final AtomicInteger sharedVariable = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        CASExample main = new CASExample();
        main.start();
    }

    private void start() throws InterruptedException {
        List<Thread> threads = new ArrayList<>(NUMBER_OF_THREADS);
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            Thread thread = new WriterThread();
            threads.add(thread);
        }

        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }

        final int expectedResult = NUMBER_OF_ITERATIONS * NUMBER_OF_THREADS;
        if (expectedResult == sharedVariable.get())
            System.out.printf("The expected result is equal to the final result " + expectedResult);
        else
            System.out.printf("The expected result " + expectedResult + " is NOT equal to the final result " + sharedVariable.get());
    }

    private class WriterThread extends Thread {

        @Override
        public void run() {
            for (int i = 0; i < NUMBER_OF_ITERATIONS; i++) {
                while (true) {
                    int currentValue = sharedVariable.get();                     // imagine that two parallel threads are at this point
                    int newValue = currentValue + 1;                             // the current value will be 0 for two threads for the very first iteration
                    if (sharedVariable.compareAndSet(currentValue, newValue))    // compareAndSet will succeed only for one thread, because for the slowest thread
                                                                                 // the value of the sharedVariable will not be equal to currentValue - 0
                                                                                 // first thread already changed it to 1
                    //sharedVariable.set(newValue);                              // try to uncomment this line and comment the line above
                        break;
                }
            }
        }
    }
}
