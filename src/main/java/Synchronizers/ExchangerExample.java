package Synchronizers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Exchanger;

public class ExchangerExample {

    public static void main(String[] args) throws InterruptedException {
        ExchangerExample main = new ExchangerExample();
        main.start();
    }

    void start() throws InterruptedException {
        Exchanger<SumTask> exchanger = new Exchanger<>();
        SummingThread thread = new SummingThread(exchanger);
        thread.start();

        SumTask task1 = new SumTask(generateArray());
        SumTask task0 = exchanger.exchange(task1);

        SumTask task2 = new SumTask(generateArray());
        task1 = exchanger.exchange(task2);

        System.out.println("The result of task1 is: " + task1.totalSum);

        task2 = exchanger.exchange(null);

        System.out.println("The result of task2 is: " + task2.totalSum);
    }

    List<Double> generateArray() {
        int arraySize = 1000;
        List<Double> array = new ArrayList<>(arraySize);
        for (int i = 0; i < arraySize; i++) {
            array.add(Math.random());
        }
        return array;
    }

    class SummingThread extends Thread {
        private final Exchanger<SumTask> exchanger;

        SummingThread(Exchanger<SumTask> exchanger) {
            this.exchanger = exchanger;
        }

        @Override
        public void run() {
            SumTask previousTask = null;
            while (true) {
                try {
                    SumTask newTask = exchanger.exchange(previousTask);
                    if (newTask == null)
                        return;

                    newTask.sum();
                    previousTask = newTask;
                } catch (InterruptedException e) {
                    interrupt();
                }
                if (isInterrupted())
                    return;
            }
        }
    }

    class SumTask {
        private final List<Double> array;
        double totalSum = 0;

        SumTask(List<Double> array) {
            this.array = array;
        }

        void sum() {
            for (Double value : array) {
                totalSum += value;
            }
        }
    }
}
