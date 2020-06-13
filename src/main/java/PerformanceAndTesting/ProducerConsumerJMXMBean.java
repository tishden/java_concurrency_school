package PerformanceAndTesting;

public interface ProducerConsumerJMXMBean {
    long getThroughput();
    long getLatency();
    void setQueueSize(int size);
    int getQueueSize();
}
