package ThreadExamples.NewThread;

public class Sales {
    private static int salesByDay[] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    public static void main(String[] args) {
        int startDay = Integer.valueOf(args[0]);
        int endDay = Integer.valueOf(args[1]);

        Sales sales = new Sales();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                sales.calculateTotal(startDay, endDay);
            }
        }, "calculation-thread");
        thread.start();
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
    }
}
