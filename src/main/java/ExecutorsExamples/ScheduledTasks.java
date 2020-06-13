package ExecutorsExamples;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

public class ScheduledTasks {
    public static void main(String[] args) throws InterruptedException {
        //timerExample();
        scheduledThreadPoolExample();
    }

    private static void scheduledThreadPoolExample() throws InterruptedException {
        ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(2);

        // 1. Simple a task scheduling
        Runnable task = () -> System.out.println("Output from the scheduled task");
        scheduledExecutor.schedule(task, 0, TimeUnit.MILLISECONDS);
        scheduledExecutor.schedule(task, 100, TimeUnit.MILLISECONDS);

        // 2. A task with exception
        ScheduledFuture<Object> resultWithException = scheduledExecutor.schedule(() -> {
            System.out.println("Starting task with exception...");
            throw new RuntimeException("An exception from the scheduled task");
        }, 0, TimeUnit.MILLISECONDS);

        // 3. A scheduled task which returns a result
        ScheduledFuture<Integer> resultFromTheTask = scheduledExecutor.schedule(() -> {
            System.out.println("Executing a task with returned result...");
            return 123;
        }, 100, TimeUnit.MILLISECONDS);

        // 4. A long running task
        scheduledExecutor.schedule(() -> {
            System.out.println("Starting a long running task..");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // will be interrupted after 1 sec
                System.out.println("The long running task was interrupted");
                return;
            }
            System.out.println("The long running task was completed");
        }, 0, TimeUnit.MILLISECONDS);

        // 5. A periodic task
        scheduledExecutor.scheduleAtFixedRate(() -> {
            System.out.println("The periodic task");
        }, 0, 200, TimeUnit.MILLISECONDS);

        Thread.sleep(1000); // wait task's competition

        try {
            Object result = resultWithException.get();
        } catch (ExecutionException e) {
            System.out.println("Caught an exception " + e.getCause());
        }

        try {
            System.out.println("Result from the scheduled task " + resultFromTheTask.get());
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        scheduledExecutor.shutdownNow();
    }

    private static void timerExample() throws InterruptedException {
        Timer timer = new Timer("timer-tread", false);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                System.out.println("Output from the timer task...");
            }
        };

        TimerTask taskWithException = new TimerTask() {
            @Override
            public void run() {
                throw new RuntimeException("An exception in a timer task...");
            }
        };

        TimerTask longTask = new TimerTask() {
            @Override
            public void run() {
                System.out.println("A long task was started...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    return;
                }
                System.out.println("A long task was completed...");
            }
        };

        //timer.schedule(taskWithException, 0, 100);   // 1. A task with exception will kill a timer's thread
        timer.schedule(longTask, 0, 100);// 2. A long executing tasks affect other tasks
        timer.schedule(task, 100, 100);
        // timer.schedule(task, 100, 100);             // 3. Can't schedule the same task twice

        Thread.sleep(500);
        task.cancel();

        Thread.sleep(1000);
        timer.cancel(); // if the timer has no daemon flag
    }
}
