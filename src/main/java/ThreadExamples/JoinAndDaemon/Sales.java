package ThreadExamples.JoinAndDaemon;

import java.time.LocalDateTime;

public class Sales {
    private static int salesByDay[] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    public static void main(String[] args) {
        int startDay = Integer.valueOf(args[0]);
        int endDay = Integer.valueOf(args[1]);

        Sales sales = new Sales();

        Thread calculationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                sales.calculateTotal(startDay, endDay);
            }
        }, "calculation-thread");

        Thread backupThread = new Thread(new Runnable() {
            @Override
            public void run() {
                sales.createBackup();
            }
        }, "backup-thread");

        calculationThread.start();

        backupThread.setDaemon(true);
        backupThread.start();

        // do some work
        try {
            calculationThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("We have got the result of work");
    }

    private void createBackup() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // writing data into the file...

            System.out.println(LocalDateTime.now() + " the backup has created");
        }
    }

    private void calculateTotal(int startDay, int endDay) {
        int salesForPeriod = 0;
        for (int i = startDay; i < endDay; i++) {
            salesForPeriod += salesByDay[i];
        }
        Thread currentThread = Thread.currentThread();
        String threadName = currentThread.getName();
        long threadId = currentThread.getId();

        System.out.println("Total sales are: " + salesForPeriod
                + ", start day is " + startDay + ", end day is " + endDay
                + ", thread id is " + threadId
                + ", thread name is " + threadName);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
