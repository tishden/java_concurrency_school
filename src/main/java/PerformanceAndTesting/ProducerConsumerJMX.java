package PerformanceAndTesting;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/*
1. JVM parameters for enabling JMX:
    -Dcom.sun.management.jmxremote
    -Dcom.sun.management.jmxremote.port=9999
    -Dcom.sun.management.jmxremote.authenticate=false
    -Dcom.sun.management.jmxremote.ssl=false
    -Dcom.sun.management.jmxremote.local.only=false

2. Monitoring allocation rate
    SJK
    https://github.com/aragozin/jvm-tools
    java -jar sjk.jar ttop -p 7609 -o ALLOC -n 10

3. Enable GC logging
    https://dzone.com/articles/enabling-and-analysing-the-garbage-collection-log
    -XX:+DisableExplicitGC
    -XX:+PrintGCDetails
    -XX:+PrintGCApplicationStoppedTime
    -XX:+PrintGCApplicationConcurrentTime
    -XX:+PrintGCDateStamps
    -Xloggc:gclog.log
    -XX:+UseGCLogFileRotation
    -XX:NumberOfGCLogFiles=5
    -XX:GCLogFileSize=2000k
4. Just In Time Compilation (JIT)
    https://www.oreilly.com/library/view/java-performance-the/9781449363512/ch04.html
    -XX:CompileThreshold=1500 -client
    -XX:CompileThreshold=10000
    -XX:+PrintCompilation
5. Java Mission Control
    https://www.youtube.com/watch?v=WMEpRUgp9Y4
 */
public class ProducerConsumerJMX implements ProducerConsumerJMXMBean {
    private static AtomicInteger producerCounter = new AtomicInteger(0);
    private static AtomicInteger consumerCounter = new AtomicInteger(0);

    private final Queue<WorkItem> workQueue = new LinkedList<>();
    private volatile int queueSize = 100;
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private final AtomicLong counter = new AtomicLong(0);
    private final AtomicReference<ConcurrentLinkedQueue<Long>> latencies = new AtomicReference<>(new ConcurrentLinkedQueue<>());
    private volatile long throughput;
    private volatile long latency;

    public static void main(String[] args) throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException {
        ProducerConsumerJMX main = new ProducerConsumerJMX();

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("PerformanceAndTesting:type=ProducerConsumerJMX");
        mbs.registerMBean(main, name);
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

        double percentile = 95;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignore
            }
            throughput = counter.getAndSet(0);
            ConcurrentLinkedQueue<Long> currentLatencies = latencies.getAndSet(new ConcurrentLinkedQueue<>());
            List<Long> sortedLatencies = currentLatencies.stream().sorted().collect(Collectors.toList());
            latency = sortedLatencies.get((int) (sortedLatencies.size() * percentile / 100.0));
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

    @Override
    public long getThroughput() {
        return throughput;
    }

    @Override
    public long getLatency() {
        return latency;
    }

    @Override
    public void setQueueSize(int size) {
        queueSize = size;
    }

    @Override
    public int getQueueSize() {
        return queueSize;
    }

    class WorkItem {
        private final long startTime;
        private long endTime = 0;
        private final String name;
        WorkItem(String name) {
            this.name = name;
            this.startTime = System.nanoTime();
        }

        void process() throws InterruptedException {

            System.out.println("Starting to process a work item " + name);

            //Thread.sleep(500);

            System.out.println("The work item " + name + " is processed in the thread " + Thread.currentThread().getId());
            counter.incrementAndGet();
            endTime = System.nanoTime();
            latencies.get().add(getLatency());
        }

        long getLatency() {
            return endTime - startTime;
        }
    }


    class Producer extends Thread {

        Producer() {
            super("Producer-" + producerCounter.getAndIncrement());
        }

        @Override
        public void run() {
            int name = 0;
            while (true) {
                lock.lock();
                try {
                    if (workQueue.size() < queueSize) {
                        WorkItem workItem = new WorkItem(String.valueOf(name) + "-" + Thread.currentThread().getId());
                        workQueue.add(workItem);
                        condition.signalAll();
                        System.out.println("Populated a work item " + name);
                        name++;
                    } else {
                        System.out.println("The queue if full trying again...");

                        try {
                            condition.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                } finally {
                    lock.unlock();
                }

                if (Thread.currentThread().isInterrupted())
                    return;
            }
        }
    }

    class Consumer extends Thread {

        Consumer() {
            super("Consumer-" + consumerCounter.getAndIncrement());
        }

        @Override
        public void run() {
            while (true) {
                WorkItem workItem;
                lock.lock();
                try {
                    while (true) {
                        workItem = workQueue.poll();
                        condition.signalAll();
                        //condition.notifyAll(); // don't do that for the condition variable
                        if (workItem == null) {
                            try {
                                condition.await();
                                //condition.wait(); // don't do that for the condition variable
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                        } else {
                            break;
                        }
                    }
                } finally {
                    lock.unlock();
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
