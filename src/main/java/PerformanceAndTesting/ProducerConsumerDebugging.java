package PerformanceAndTesting;

import java.util.LinkedList;
import java.util.Queue;

public class ProducerConsumerDebugging {

    private final Queue<WorkItem> workQueue = new LinkedList<>();
    private final int queueSize = 100;
    private final Object lock = new Object();

    /*
        1. Remote debugging:
        -Xdebug -runjdwp:transport=dt_socket, server=y, suspend=n, address=<debug-port> <ClassName>

        server=y – Java application should be a TCP/IP server and wait for connections
        suspend=y – the Java application (now server) will pause on start up and wait for debugger to join. If ‘n‘, application will not wait for debugger at start up, instead it will run as usual and when IDE encounters breakpoint it will fork the application.
        address=<debug-port> – a port number on which the debugger will join to debug the application. Remember, this should be a port that is not already in use.
        ClassName – Java application main-class name. If we have a jar we can include that with appropriate classpath.

        2. Logging with log4j2

        3. Stacktraces an Threads Dump with jstack
        https://docs.oracle.com/javase/8/docs/technotes/guides/troubleshoot/tooldescr016.html

        4. Heap dump with jmap
        https://docs.oracle.com/javase/7/docs/technotes/tools/share/jmap.html
        -XX:+HeapDumpOnOutOfMemoryError

        5. Analyzing a heap dump with Eclipse MAT
        https://www.eclipse.org/mat/
     */
    public static void main(String[] args) {
        ProducerConsumerDebugging main = new ProducerConsumerDebugging();
        while (true)
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
                synchronized (lock) {
                    if (workQueue.size() < queueSize) {
                        WorkItem workItem = new WorkItem(String.valueOf(name) + "-" + Thread.currentThread().getId());
                        workQueue.add(workItem);
                        lock.notifyAll();
                        System.out.println("Populated a work item " + name);
                        name++;
                    } else {
                        System.out.println("The queue if full trying again...");

                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
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
                synchronized (lock) {
                    while (true) {
                        workItem = workQueue.poll();
                        lock.notifyAll();

                        if (workItem == null) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                        } else {
                            break;
                        }
                    }
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
