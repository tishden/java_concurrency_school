package ExecutorsExamples;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ForkJoinPoolExample {
    static final int batchSize = 100;

    public static void main(String[] args) {
        while (true) {
            int size = 10_000;
            List<Double> elements = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                elements.add((double) i);
            }

            //ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
            ForkJoinPool forkJoinPool = new ForkJoinPool(3);
            Double result = forkJoinPool.invoke(new SumTask(elements, 0, size));
            //double totalSum = sum(elements, 0, size);
            forkJoinPool.shutdownNow();
            System.out.println("Total sum is: " + result);
        }
    }

    static double sum(List<Double> array, int startIndex, int endIndex) {
        System.out.println("Thread name is " + Thread.currentThread().getName());
        int elementsCount = endIndex - startIndex;
        if (elementsCount <= batchSize) {
            double sum = 0.0;
            for (int i = startIndex; i < endIndex; i++) {
                sum += array.get(i);
            }
            return sum;
        } else {
            int batchElementsCount = elementsCount / 2;
            SumTask sumTask1 = new SumTask(array, startIndex, startIndex + batchElementsCount);
            SumTask sumTask2 = new SumTask(array, startIndex + batchElementsCount, endIndex);

            sumTask1.fork();
            sumTask2.fork();

            return sumTask1.join() + sumTask2.join();
            //double partialSum1 = sum(array, startIndex, startIndex + batchElementsCount);
            //double partialSum2 = sum(array, startIndex + batchElementsCount, endIndex);
            //return partialSum1 + partialSum2;
        }
    }

    static class SumTask extends RecursiveTask<Double> {
        final List<Double> array;
        final int startIndex;
        final int endIndex;

        SumTask(List<Double> array, int startIndex, int endIndex) {
            this.array = array;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        @Override
        protected Double compute() {
            return sum(array, startIndex, endIndex);
        }
    }
}
