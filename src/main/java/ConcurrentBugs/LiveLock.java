package ConcurrentBugs;

public class LiveLock {

    private volatile boolean hostageIsReleased;
    private volatile boolean moneyIsSent;

    public static void main(String[] args) throws InterruptedException {
        LiveLock main = new LiveLock();
        main.start();
    }

    void start() throws InterruptedException {
        Police police = new Police();
        Criminal criminal = new Criminal();

        police.start();
        criminal.start();

        police.join();
        criminal.join();
    }

    class Police extends Thread {
        @Override
        public void run() {
            while (true) {
                if (hostageIsReleased) {
                    sendMoney();
                    break;
                }
            }
        }
    }

    class Criminal extends Thread {
        @Override
        public void run() {
            while (true) {
                if (moneyIsSent) {
                    releaseHostage();
                    break;
                }
            }
        }
    }

    void sendMoney() {
        moneyIsSent = true;
    }

    void releaseHostage() {
        hostageIsReleased = true;
    }
}
