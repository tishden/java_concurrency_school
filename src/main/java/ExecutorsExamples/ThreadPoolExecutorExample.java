package ExecutorsExamples;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolExecutorExample {
    public static void main(String[] args) {
        BlockingQueue<Runnable> tasksQueue = new LinkedBlockingQueue<Runnable>(1000);
        ThreadPoolExecutor customExecutor =
                new ThreadPoolExecutor(
                        3,
                        10,
                        1,
                        TimeUnit.SECONDS,
                        tasksQueue,
                        new CustomThreadFactory("custom-thread-factory-"));
                        //Executors.defaultThreadFactory());

        customExecutor.submit(() -> {
            System.out.println("Thread name is: " + Thread.currentThread().getName());
        });

        customExecutor.execute(() -> {
            System.out.println("Thread name is: " + Thread.currentThread().getName());
            throw new RuntimeException("An exception from the submitted task");
        });

        //customExecutor.getActiveCount()
        //customExecutor.getTaskCount()
        //customExecutor.getQueue()

        customExecutor.shutdown();
    }

    static class CustomThreadFactory implements ThreadFactory {
        final String threadNamePatter;
        final AtomicInteger counter = new AtomicInteger(0);

        CustomThreadFactory(String threadNamePatter) {
            this.threadNamePatter = threadNamePatter;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, threadNamePatter + counter.getAndIncrement());
            thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    System.out.println("Uncaught Exception: " + e);
                }
            });
            return thread;
        }
    }
}
