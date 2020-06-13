package ExecutorsExamples;

import java.util.concurrent.*;

public class BasicExecutors {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10); // a fixed pool
        Executors.newSingleThreadExecutor(); // a single thread
        Executors.newCachedThreadPool(); // creates new threads as needed, or reuse previously constructed
        Executors.newScheduledThreadPool(10); // can schedule commands to run after a given delay, or to execute periodically

        // 1. We don't care about result
        executor.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("Executing a task in the ExecutorService " + Thread.currentThread().getName());
            }
        });

        // 2. We don't care about result, but we have to be sure that the task is completed
        Future<?> result = executor.submit(new Runnable() {
            @Override
            public void run() {
                System.out.println("Submitted task in the ExecutorService " + Thread.currentThread().getName());
            }
        });

        System.out.println("Is the asynchronous done " + result.isDone());

        Object asyncTaskResult;
        try {
            asyncTaskResult = result.get(1, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        asyncTaskResult = result.get();

        System.out.println("The result of the asynchronous Runnable task is: " + asyncTaskResult);

        // 3. An asynchronous task with an exception
        Future<?> resultWithEx = executor.submit(new Runnable() {
            @Override
            public void run() {
                throw new RuntimeException("Exception from the asynchronous task");
            }
        });

        try {
            resultWithEx.get();
        } catch (ExecutionException e) {
            System.out.println("Caught an ExecutionException, the original exception is: " + e.getCause());
        }

        // 4. An asynchronous task with a valuable result
        Future<Integer> valuableResult = executor.submit(new Callable<Integer>() {
            @Override
            public Integer call() {
                return 1234;
            }
        });

        // valuableResult.cancel(); if you want to cancel the running task

        System.out.println("The result of the asynchronous Callable task is: " + valuableResult.get());

        // 5. A long running tasks
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    System.out.println("A long running task...");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        });
        Thread.sleep(1000);
        //executor.shutdown();
        executor.shutdownNow(); // throw an Interrupt exception
        //executor.awaitTermination();
    }
}
