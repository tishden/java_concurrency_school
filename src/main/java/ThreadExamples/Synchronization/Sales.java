package ThreadExamples.Synchronization;

import java.time.LocalDateTime;

public class Sales {
    private static int salesByDay[] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    private long totalSales = 0; // a shared variable

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        int startDay = Integer.valueOf(args[0]);
        int endDay = Integer.valueOf(args[1]);
        int daysPerThread = (int)Math.ceil((endDay - startDay) / 2.0);
        Sales sales = new Sales();

        Thread calculationThread1 = new CalculationThread(sales, startDay, daysPerThread + startDay, "calculation-thread-1");
        Thread calculationThread2 = new CalculationThread(sales, daysPerThread + startDay, endDay, "calculation-thread-2");

        Thread backupThread = new Thread(new Runnable() {
            @Override
            public void run() {
                sales.createBackup();
            }
        }, "backup-thread");

        calculationThread1.start();
        calculationThread2.start();

        backupThread.start();

        // do some work
        try {
            calculationThread1.join();
            calculationThread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        backupThread.interrupt();
        try {
            backupThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println("Total sales are: " + sales.totalSales
                + ", execution of the program took: " + totalTime + " ms.");
    }

    private void createBackup() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("The backup thread is interrupted");
                return;
            }

            // writing data into the file...

            System.out.println(LocalDateTime.now() + " the backup has created");

            if (Thread.currentThread().isInterrupted())
                return;
        }
    }

    synchronized public void addPartialSales(long partialSales) {
        // this.monitor.lock
        System.out.println("A monitor of the object " + this + " is locked by the thread "
                + Thread.currentThread().getName());

        totalSales += partialSales;

        System.out.println("A monitor of the object " + this + " is unlocked by the thread "
                + Thread.currentThread().getName());
        // this.monitor.unlock
    }

    synchronized private void printCurrentDate() {
        System.out.println("A monitor of the object " + this + " is locked by the thread "
                + Thread.currentThread().getName());

        System.out.println(LocalDateTime.now());

        System.out.println("A monitor of the object " + this + " is unlocked by the thread "
                + Thread.currentThread().getName());
    }

    private void calculateTotal(int startDay, int endDay) {
        int salesForPeriod = 0;
        for (int i = startDay; i < endDay; i++) {
            salesForPeriod += salesByDay[i];

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println("The thread was interrupted");
            }
        }
        addPartialSales(salesForPeriod);

        Thread currentThread = Thread.currentThread();
        String threadName = currentThread.getName();
        long threadId = currentThread.getId();

        System.out.println("Partial sales are: " + salesForPeriod
                + ", start day is " + startDay + ", end day is " + endDay
                + ", thread id is " + threadId
                + ", thread name is " + threadName);
    }

    static class CalculationThread extends Thread {
        final Sales sales;
        final int startDay;
        final int endDay;

        CalculationThread(Sales sales, int startDay, int endDay, String threadName) {
            super(threadName);
            this.sales = sales;
            this.startDay = startDay;
            this.endDay = endDay;
        }

        @Override
        public void run() {
            sales.calculateTotal(startDay, endDay);
        }
    }
}
