package CollectionsExamples;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class MapReduce {
    private final BlockingQueue<String> linesQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<KeyValues> keyValuesQueue = new LinkedBlockingQueue<>();
    private final ConcurrentMap<String, List<Integer>> keyValues = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Integer> reduceResult = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException, InterruptedException {
        MapReduce main = new MapReduce();
        main.start();
    }

    void start() throws IOException, InterruptedException {
        MapThread map1 = new MapThread();
        MapThread map2 = new MapThread();

        ReduceThread reduce1 = new ReduceThread();
        ReduceThread reduce2 = new ReduceThread();

        map1.start();
        map2.start();

        reduce1.start();
        reduce2.start();

        BufferedReader reader = new BufferedReader(
                new FileReader(getClass().getClassLoader().getResource("LordOfTheRings.txt").getFile()));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
                linesQueue.put(line);
            }
        }

        isReady();

        map1.interrupt();
        map2.interrupt();

        reduce1.interrupt();
        reduce2.interrupt();

        List<Map.Entry<String, Integer>> theMostPopularWords = reduceResult.entrySet()
                .stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .limit(100)
                .collect(Collectors.toList());

        theMostPopularWords.forEach(System.out::println);
    }

    void isReady() throws InterruptedException {
        while (!linesQueue.isEmpty() || !keyValuesQueue.isEmpty())
            Thread.sleep(3000);
    }

    List<KeyValue> map(String line) {
        String[] words = line.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
        List<KeyValue> keyValueList = new ArrayList<>();
        for (String word : words) {
            keyValueList.add(new KeyValue(word, 1));
        }
        return keyValueList;
    }

    Integer reduce(KeyValues keyValues) {
        if (keyValues.key.length() < 4)
            return 0;

        int sum = 0;
        for (Integer value : keyValues.values) {
            sum += value;
        }
        return sum;
    }

    void prepareForReduce(KeyValue keyValue) {
        String key = keyValue.key;
        keyValues.compute(key, (word, values) -> {
           if (values == null) {
               List<Integer> wordsCount = new ArrayList<>();
               wordsCount.add(keyValue.value);
               return wordsCount;
           } else {
               values.add(keyValue.value);
               if (values.size() > 100) {
                   publishForReduce(key, values);
                   return new ArrayList<>();
               } else {
                   return values;
               }
           }
        });
    }

    private void publishForReduce(String key, List<Integer> values) {
        if (!values.isEmpty()) {
            try {
                keyValuesQueue.put(new KeyValues(key, values));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void setResult(String key, Integer result) {
        reduceResult.compute(key, (k, currentValue) -> {
            if (currentValue == null) {
                return result;
            } else {
                return reduce(new KeyValues(key, Arrays.asList(result, currentValue)));
            }
        });
    }

    static class KeyValue {
        final String key;
        final Integer value;

        KeyValue(String key, Integer value) {
            this.key = key;
            this.value = value;
        }
    }

    static class KeyValues {
        final String key;
        final List<Integer> values;

        KeyValues(String key, List<Integer> values) {
            this.key = key;
            this.values = values;
        }
    }

    class MapThread extends Thread {
        @Override
        public void run() {
            while (!isInterrupted()) {
                String line;
                try {
                    line = linesQueue.poll(1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    return;
                }

                if (line == null) {
                    keyValues.replaceAll((key, values) -> {
                        publishForReduce(key, values);
                        return new ArrayList<>();
                    });
                    continue;
                }
                List<KeyValue> keyValuesList = map(line);
                keyValuesList.forEach((k) -> prepareForReduce(k));
            }
        }
    }

    class ReduceThread extends Thread {
        @Override
        public void run() {
            while (!isInterrupted()) {
                KeyValues keyValues;
                try {
                    keyValues = keyValuesQueue.take();
                } catch (InterruptedException e) {
                    return;
                }
                Integer result = reduce(keyValues);
                setResult(keyValues.key, result);
            }
        }
    }
}
