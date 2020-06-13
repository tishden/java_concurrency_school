package ExecutorsExamples;

import java.util.concurrent.*;

public class CompletableFutureExample {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        // 1. thenApply/thenApplyAsync transforms a result
        CompletableFuture<Integer> futureResult1 = CompletableFuture.supplyAsync(() -> 2 + 2);
        CompletableFuture<Integer> futureResult2 = futureResult1.thenApplyAsync((result) -> result + 3);
        CompletableFuture<String> futureResult3 = futureResult2.thenApplyAsync((result) -> "Transformed value is: " + result);

        int result1 = futureResult1.get();
        int result2 = futureResult2.get();
        String result3 = futureResult3.get();

        System.out.println(result1 + " " + result2 + " " + result3);

        // 2. thenAcceptAsync/thenRunAsync runs some code after completion of the whole chain of futures
        CompletableFuture<Void> chainedResult = CompletableFuture.supplyAsync(() -> 2 + 2)
                .thenApplyAsync((result) -> result + 3)
                .thenApplyAsync((result) -> "Transformed value is: " + result)
                .thenAcceptAsync((result) -> System.out.println(result))
                .thenRunAsync(() -> System.out.println("The chain execution was completed"));

        chainedResult.get();

        // 3. thenCombine/thenCombineAsync combine results of two futures
        CompletableFuture<Integer> future1 = CompletableFuture.completedFuture(11);
        CompletableFuture<Integer> future2 = new CompletableFuture<>();
        // doing some computation
        future2.complete(22);
        CompletableFuture<Void> combinedResult = future1.thenCombineAsync(future2, (f1, f2) -> f1 + f2)
                .thenAcceptAsync(System.out::println);
        combinedResult.get();

        // 4. thenCompose/thenComposeAsync extracts a nested CompletableFuture
        CompletableFuture<Integer> res1 = CompletableFuture.supplyAsync(() -> 3 + 3)
                .thenComposeAsync((value) -> someFunctionWithCompletableFutureResult(value));

        // 5. thenAcceptBothAsync/runAfterBothAsync waits for completion of both futures
        future1.thenAcceptBothAsync(future2, (f1, f2) -> System.out.println(f1 + f2));
        future1.runAfterBothAsync(future2, () -> System.out.println("something..."));

        // 6. waits for the first available result from the future1 or future2
        future1.acceptEitherAsync(future2, (f) -> System.out.println(f));
        future1.applyToEitherAsync(future2, (f) -> f + f);
        future1.runAfterEitherAsync(future2, () -> System.out.println("something..."));

        // 7.1 allOf waits for all futures
        CompletableFuture<Void> allOfFuture = CompletableFuture.allOf(future1, future2);
        allOfFuture.get();

        // 7.2 anyOf waits for the first available future
        CompletableFuture<Object> anyOfResult = CompletableFuture.anyOf(future1, future2);
        anyOfResult.get();

        // 8 exceptionally or handle methods for handling an exception in a future object
        CompletableFuture<Integer> res2 = CompletableFuture.supplyAsync(() -> someFunctionWithException(-55))
                .exceptionally((e) -> {
                    System.out.println("Caught an exception");
                    return 0;
                });
        System.out.println(res2.get());

        CompletableFuture<Integer> res3 = CompletableFuture.supplyAsync(() -> someFunctionWithException(66))
                .handle((value, ex) -> {
                   if (ex != null) {
                       System.out.println("Caught an exception in the handle method");
                       return 0;
                   } else {
                       return value;
                   }
                });
        System.out.println(res3.get());

        // 9. Complete a future with exception
        CompletableFuture<Integer> future3 = new CompletableFuture<>();
        future3.completeExceptionally(new RuntimeException());
        //future3.cancel();

        // 10. Custom ExecutorService
        ExecutorService customThreadPool = Executors.newFixedThreadPool(2);
        CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName());
            return 33;
        }, customThreadPool);
        f1.get();
        customThreadPool.shutdownNow();
    }

    static int someFunctionWithException(int value) {
        if (value < 0) {
            throw new RuntimeException("The value less the 0");
        }

        return value * value;
    }
    static CompletableFuture<Integer> someFunctionWithCompletableFutureResult(int value) {
        return CompletableFuture.completedFuture(value * value);
    }

    void simpleFutureExample() throws ExecutionException, InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        Future<Integer> futureResult1 = threadPool.submit(() -> {
            return 2 + 2;
        });

        int additionalWork = 4 + 4;

        Future<Integer> futureResult2 = threadPool.submit(() -> {
            return 3 + 3;
        });

        Integer resultFromTheFuture1 = futureResult1.get();
        Integer resultFromTheFuture2 = futureResult2.get();

        Future<Integer> mergedResultFuture = threadPool.submit(() -> {
            return resultFromTheFuture1 + resultFromTheFuture2;
        });

        Integer result = mergedResultFuture.get();
    }
}
