package CollectionsExamples;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class ConcurrentMapExample {

    final ConcurrentMap<SubscriptionId, SubscriptionContext> subscriptionToContext = new ConcurrentHashMap<>();
    //Set<SubscriptionId> set = ConcurrentHashMap.newKeySet();
    //ConcurrentSkipListMap;
    //ConcurrentSkipListSet;

    public static void main(String[] args) throws InterruptedException {
        ConcurrentMapExample main = new ConcurrentMapExample();
        main.start();
    }

    void start() throws InterruptedException {
        UserThread user1 = new UserThread("usr-1");
        UserThread user2 = new UserThread("usr-2");
        UserThread user3 = new UserThread("usr-3");

        UpdatingThread update1 = new UpdatingThread();
        UpdatingThread update2 = new UpdatingThread();

        user1.start();
        user2.start();
        user3.start();

        update1.start();
        update2.start();

        user1.join();
        user2.join();
        user3.join();

        update1.interrupt();
        update2.interrupt();
    }

    void createSubscription(SubscriptionId id) {
        SubscriptionContext context = new SubscriptionContext("context-" + id.id, 0);
        subscriptionToContext.put(id, context);
    }

    void updateSubscription(SubscriptionId id) {
        subscriptionToContext.compute(id, (subscriptionId, currentContext) -> {
            if (currentContext == null)
                return null;
            SubscriptionContext context = new SubscriptionContext(currentContext.name, currentContext.version + 1);
            System.out.println("The subscription " + subscriptionId.id
                    + " is updated, the new version of the context is " + context.name + "#" + context.version);
            return context;
        });
    }

    void closeSubscription(SubscriptionId id) {
        subscriptionToContext.remove(id);
    }

    private class SubscriptionId {
        final String id;

        private SubscriptionId(String id) {
            this.id = id;
        }
    }

    private class SubscriptionContext {
        final String name;
        final int version;

        private SubscriptionContext(String name, int version) {
            this.name = name;
            this.version = version;
        }
    }

    class UserThread extends Thread {
        final String id;

        UserThread(String id) {
            this.id = id;
        }

        @Override
        public void run() {
            SubscriptionId subscriptionId = new SubscriptionId(id);
            createSubscription(subscriptionId);

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return;
            }

            closeSubscription(subscriptionId);
        }
    }

    class UpdatingThread extends Thread {
        @Override
        public void run() {
            while (!isInterrupted()) {
                subscriptionToContext.forEach((k, v) -> updateSubscription(k));
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }
}
