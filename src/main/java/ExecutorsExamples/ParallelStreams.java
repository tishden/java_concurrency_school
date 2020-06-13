package ExecutorsExamples;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.Collectors;

public class ParallelStreams {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        List<Double> array = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            array.add(Math.random());
        }

        List<Integer> transformedArray = array.parallelStream() // possibly will be parallelized
                .map(e -> {
                    System.out.println("parallelStream thread name " + Thread.currentThread().getName());
                    return (int) (e * 100);
                })
                .sorted()
                .filter(e -> e > 20)
                .collect(Collectors.toList());

        ForkJoinPool fjPool = new ForkJoinPool(2); // using a custom ForkJoinPool pool
        ForkJoinTask<Long> countOfElements = fjPool.submit(() -> {
            return array.stream()
                    .parallel() // the stream definitely will be parallelized
                    .map(e -> {
                        System.out.println("parallel stream thread name " + Thread.currentThread().getName());
                        return (int) (e * 100);
                    })
                    //.sequential() // makes a parallel thread sequential
                    //.isParallel()
                    .filter(e -> e > 20)
                    .count();
        });


        Optional<Integer> maxElementOptional = array.stream()
                .map(e -> {
                    System.out.println("sequential stream thread name " + Thread.currentThread().getName());
                    return (int) (e * 100);
                })
                .filter(e -> e > 20)
                .max(Integer::compareTo);

        System.out.println(array);
        System.out.println(transformedArray);
        System.out.println("Count of elements: " + countOfElements.get());
        System.out.println("The maximum element is: " + maxElementOptional.get());
    }
}
