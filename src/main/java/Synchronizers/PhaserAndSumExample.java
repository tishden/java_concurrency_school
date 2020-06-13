package Synchronizers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Phaser;

public class PhaserAndSumExample {

    private static final int NUMBER_OF_THREADS = 4;

    private final int PHASES_COUNT = 3;

    private final List<Integer> soldItems = new ArrayList<>(); // sum ~ 100%
    private final List<Double> soldItemsPercents = new ArrayList<>(); // e * 100 / sum

    private int totalSum = 0;
    private int max = 0;
    private int min = 0;

    private final List<Worker> workers = new ArrayList<>();
    private final Phaser phaser = new Phaser(NUMBER_OF_THREADS) {

        @Override
        protected boolean onAdvance(int phase, int registeredParties) {
            System.out.println("The Phase " + phase + " is completed");
            if (phase == 0) {
                for (Worker worker : workers) {
                    totalSum += worker.sum;
                }
            } if (phase == 1) {
                for (Worker worker : workers) {
                    soldItemsPercents.addAll(worker.partialArrayPercents);
                }
            }
            return phase >= PHASES_COUNT - 1 || registeredParties == 0;
        }
    };

    public static void main(String[] args) throws InterruptedException {
        PhaserAndSumExample main = new PhaserAndSumExample();
        main.start();
    }

    void start() throws InterruptedException {
        for (int i = 0; i < 1000; i++) {
            soldItems.add((int)(100.0 * Math.random()));
        }

        List<List<Integer>> partialArrays = splitArray(soldItems, (soldItems.size() / NUMBER_OF_THREADS) + 1);
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            String name;
            if (i == 0)
                name = "min";
            else if (i == 1)
                name = "max";
            else
                name = "worker-" + i;

            Worker thread = new Worker(partialArrays.get(i), name);
            workers.add(thread);
            thread.start();
        }

        for (int phase = 0; phase < PHASES_COUNT; phase++)
            phaser.awaitAdvance(phase);

        /*for (Worker worker : workers) {
            worker.join();
        }*/

        double sum = 0;
        for (Double soldItemsPercent : soldItemsPercents) {
            sum += soldItemsPercent;
        }
        System.out.println(sum + " " + min + " " + max);
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

        Worker(List<Integer> partialArray, String name) {
            super(name);
            this.partialArray = partialArray;
        }

        @Override
        public void run() {
            for (Integer soldItems : partialArray) {
                sum += soldItems;
            }

            int phase = phaser.arriveAndAwaitAdvance() - 1;
            System.out.println("The Phase " + phase + " is completed in the thread " + getName());

            for (int i = 0; i < partialArray.size(); i++) {
                //e * 100 / sum
                partialArrayPercents.add((double) partialArray.get(i) * 100.0 / (double)totalSum);
            }

            if (getName().equals("min")) {
                phase = phaser.arriveAndAwaitAdvance() - 1;
                System.out.println("The Phase " + phase + " is completed in the thread " + getName());
                min = soldItems.stream().min(Integer::compareTo).get();
            } else if (getName().equals("max")) {
                phase = phaser.arriveAndAwaitAdvance() - 1;
                System.out.println("The Phase " + phase + " is completed in the thread " + getName());
                max = soldItems.stream().max(Integer::compareTo).get();
            }
            phase = phaser.arriveAndDeregister();
            System.out.println("The Phase " + phase + " is completed in the thread " + getName());
        }
    }
}
