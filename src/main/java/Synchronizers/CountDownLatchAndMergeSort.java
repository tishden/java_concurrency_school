package Synchronizers;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CountDownLatchAndMergeSort {
    /*
        You can play with batch size and threads count parameters and check,
        that not every combination works fast
     */
    private static final int BATCH_SIZE = 10000;
    private static final int WORKING_THREADS_COUNT = 4;

    private final List<Thread> workingThreads = new ArrayList<>(WORKING_THREADS_COUNT);
    private final int queueSize = 100;
    private final Lock sortingQueueLock = new ReentrantLock();
    private final Condition sortingQueueCondition = sortingQueueLock.newCondition();
    private final Queue<SortingTask> sortingQueue = new LinkedList<>();

    public static void main(String[] args) throws InterruptedException {
        CountDownLatchAndMergeSort sorter = new CountDownLatchAndMergeSort();
        for (int k = 0; k < 10; k++) { // do several iterations, because the first run works slowly in JAVA
            int arraySize = 100000;
            List<Integer> array = new ArrayList<>(arraySize);
            for (int i = 0; i < arraySize; i++) {
                array.add((int) (100.0 * Math.random()));
            }
            MergeTask sortingTask = sorter.sort(array);
            List<Integer> sortedArray = sortingTask.getResult(); // if you want, you can run the merge stage in a separate thread
            if (checkSortedArray(array, sortedArray))
                System.out.println("Array is successfully sorted");
            else
                System.out.println("Array is NOT sorted...");

            //System.out.println("Base array: " + array);
            //System.out.println("Sorted array: " + sortedArray);
        }
    }

    public CountDownLatchAndMergeSort() {
        for (int i = 0; i < WORKING_THREADS_COUNT; i++) {
            SortTask workingThread = new SortTask();
            workingThreads.add(workingThread);
            workingThread.setDaemon(true); // use daemon threads
            workingThread.start();
        }
    }

    static private boolean checkSortedArray(List<Integer> baseArray, List<Integer> sortedArray) {
        if (baseArray.size() != sortedArray.size()) {
            System.out.println("Array sizes are not equal, baseArray size: "
                    + baseArray.size()
                    + ",  sortedArray size: "
                    + sortedArray.size());
            return false;
        }

        for (int i = 1; i < sortedArray.size(); i++) {
            if (sortedArray.get(i) < sortedArray.get(i - 1)) {
                return false;
            }
        }
        return true;
    }

    public MergeTask sort(List<Integer> array) throws InterruptedException {
        List<List<Integer>> splittedArray = splitArray(array, BATCH_SIZE);
        CountDownLatch latch = new CountDownLatch(splittedArray.size());
        MergeTask mergeTask = new MergeTask(latch, splittedArray);
        sortingQueueLock.lock();
        try {
            for (List<Integer> partilArray : splittedArray) {
                while (sortingQueue.size() >= queueSize) // protecting from spurious wakeup
                    sortingQueueCondition.await();

                sortingQueue.add(new SortingTask(latch, partilArray));
                sortingQueueCondition.signalAll();
            }
        } finally {
            sortingQueueLock.unlock();
        }
        return mergeTask;
    }

    static private List<List<Integer>> splitArray(List<Integer> array, int bySize) {
        List<List<Integer>> splittedArray = new ArrayList<>(array.size() / bySize);
        List<Integer> partialArrays = new ArrayList<>(bySize);
        for (Integer partialArray : array) {
            partialArrays.add(partialArray);
            if (partialArrays.size() == bySize) {
                splittedArray.add(partialArrays);
                partialArrays = new ArrayList<>(bySize);
            }
        }

        if (partialArrays.size() > 0)
            splittedArray.add(partialArrays);

        return splittedArray;
    }

    private class SortingTask {
        private final CountDownLatch latch;
        private final List<Integer> array;

        private SortingTask(CountDownLatch latch, List<Integer> array) {
            this.latch = latch;
            this.array = array;
        }

        void process() {
            Collections.sort(array);
            latch.countDown(); // notifying, that sorting task is completed and ready to process a new task
        }
    }

    private class MergeTask {
        private final CountDownLatch latch;
        private final List<List<Integer>> arrays;
        private final Stopwatch stopwatch;

        private MergeTask(CountDownLatch latch, List<List<Integer>> arrays) {
            this.latch = latch;
            this.arrays = arrays;
            this.stopwatch = new Stopwatch();
            stopwatch.start();
        }

        List<Integer> getResult() throws InterruptedException {
            latch.await(); // awaiting competition of all sorting tasks
            List<Integer> resultArray = new ArrayList<>();
            List<Integer> arraysIndexes = new ArrayList<>(arrays.size());
            for (int i = 0; i < arrays.size(); i++) {
                arraysIndexes.add(0);
            }

            boolean haveValues = true;
            while (haveValues) {
                haveValues = false;
                Integer minValue = null;
                int arrayIndexWithMinValue = 0;
                for (int arrayIndex = 0; arrayIndex < arrays.size(); arrayIndex++) {
                    if (arraysIndexes.get(arrayIndex) >= arrays.get(arrayIndex).size()) {
                        continue;
                    }
                    haveValues = true;

                    Integer arrayValue = arrays.get(arrayIndex).get(arraysIndexes.get(arrayIndex));
                    if (minValue == null || arrayValue <= minValue) {
                        arrayIndexWithMinValue = arrayIndex;
                        minValue = arrayValue;
                    }
                }
                if (haveValues) {
                    resultArray.add(minValue);
                    arraysIndexes.set(arrayIndexWithMinValue, arraysIndexes.get(arrayIndexWithMinValue) + 1);
                }
            }
            long sortingTime = stopwatch.stop();
            System.out.println("The array of " + resultArray.size() + " elements sorted in " + sortingTime + " ms.");
            return resultArray;
        }
    }

    private class SortTask extends Thread {

        SortTask() {
            super("sorter-thread");
        }

        @Override
        public void run() {
            while (true) {
                SortingTask sortingTask;
                sortingQueueLock.lock();
                try {
                    sortingTask = sortingQueue.poll();
                    if (sortingTask == null) {
                        try {
                            sortingQueueCondition.await();
                        } catch (InterruptedException e) {
                            interrupt();
                        }
                    }
                } finally {
                    sortingQueueLock.unlock();
                }

                if (isInterrupted())
                    return;

                if (sortingTask != null) {
                    sortingTask.process();
                }
            }
        }
    }

    private static class Stopwatch {
        long startTime = 0;

        void start() {
            startTime = System.currentTimeMillis();
        }

        long stop() {
            if (startTime == 0) {
                throw new RuntimeException("Stopwatch is not started");
            }
            return System.currentTimeMillis() - startTime;
        }
    }
}
