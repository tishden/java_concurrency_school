package Synchronizers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class CyclicBarrierAndSumExample {

    private static final int NUMBER_OF_THREADS = 4;

    private final List<Integer> soldItems = new ArrayList<>(); // sum ~ 100%
    private final List<Double> soldItemsPercents = new ArrayList<>(); // e * 100 / sum

    private int totalSum = 0;
    private final List<Worker> workers = new ArrayList<>();
    private final CyclicBarrier barrierForSum = new CyclicBarrier(NUMBER_OF_THREADS, () -> {
        for (Worker worker : workers) {
            totalSum += worker.sum;
        }
    });

    private final CyclicBarrier barrierForPercents = new CyclicBarrier(NUMBER_OF_THREADS, () -> {
        for (Worker worker : workers) {
            soldItemsPercents.addAll(worker.partialArrayPercents);
        }
    });

    public static void main(String[] args) throws InterruptedException {
        CyclicBarrierAndSumExample main = new CyclicBarrierAndSumExample();
        main.start();
    }

    void start() throws InterruptedException {
        for (int i = 0; i < 1000; i++) {
            soldItems.add((int)(100.0 * Math.random()));
        }

        List<List<Integer>> partialArrays = splitArray(soldItems, (soldItems.size() / NUMBER_OF_THREADS) + 1);
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            Worker thread = new Worker(partialArrays.get(i));
            workers.add(thread);
            thread.start();
        }

        for (Worker worker : workers) {
            worker.join();
        }

        double sum = 0;
        for (Double soldItemsPercent : soldItemsPercents) {
            sum += soldItemsPercent;
        }
        System.out.println(sum);
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

    class Worker extends Thread {

        final List<Integer> partialArray;
        final List<Double> partialArrayPercents = new ArrayList<>();
        int sum = 0;

        Worker(List<Integer> partialArray) {
            this.partialArray = partialArray;
        }

        @Override
        public void run() {
            for (Integer soldItems : partialArray) {
                sum += soldItems;
            }

            try {
                barrierForSum.await();
            } catch (InterruptedException e) {
                interrupt();
            } catch (BrokenBarrierException e) {
                System.out.println(e);
                interrupt();
            }

            if(isInterrupted())
                return;

            for (int i = 0; i < partialArray.size(); i++) {
                //e * 100 / sum
                partialArrayPercents.add((double)partialArray.get(i) * 100.0 / (double)totalSum);
            }

            try {
                barrierForPercents.await();
            } catch (InterruptedException e) {
                interrupt();
            } catch (BrokenBarrierException e) {
                System.out.println(e);
                interrupt();
            }
        }
    }
}
