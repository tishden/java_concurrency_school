package ThreadExamples.Main;

public class Sales {
    private static int salesByDay[] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    public static void main(String[] args) {
        int startDay = Integer.valueOf(args[0]);
        int endDay = Integer.valueOf(args[1]);

        Sales sales = new Sales();
        sales.calculateTotals(startDay, endDay);
    }

    private void calculateTotals(int startDay, int endDay) {
        int salesForPeriod = 0;
        for (int i = startDay; i < endDay; i++) {
            salesForPeriod += salesByDay[i];
        }
        System.out.println("Total sales are: " + salesForPeriod
                + ", start day is " + startDay + ", end day is " + endDay);
    }
}
