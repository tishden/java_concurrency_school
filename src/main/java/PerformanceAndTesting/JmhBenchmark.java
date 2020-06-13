package PerformanceAndTesting;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/*
    JMH Home Page - https://openjdk.java.net/projects/code-tools/jmh/
    More Examples - https://hg.openjdk.java.net/code-tools/jmh/file/tip/jmh-samples/src/main/java/org/openjdk/jmh/samples/
    Documentation - http://javadox.com/org.openjdk.jmh/jmh-core/1.7/org/openjdk/jmh/annotations/package-summary.html
 */
public class JmhBenchmark {
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JmhBenchmark.class.getSimpleName())
                .forks(1)
                .build();
        new Runner(opt).run();
    }

    /*
        ScopeBenchmark - All threads running the benchmark share the same state object.
        ScopeThread - Each thread running the benchmark will create its own instance of the state object.
        ScopeGroup - Each thread group running the benchmark will create its own instance of the state object.
    */
    @State(Scope.Benchmark)
    public static class MyState {
        @Param({"2", "5"})
        public int iterations;
        public final int c = 3;
        public int sum;

        /*
           Level.Trial	- The method is called once for each time for each full run of the benchmark. A full run means a full "fork" including all warmup and benchmark iterations.
           Level.Iteration	- The method is called once for each iteration of the benchmark.
           Level.Invocation - The method is called once for each call to the benchmark.
        */
        @Setup(Level.Iteration)
        public void doSetup() {
            sum = 0;
        }

        @TearDown(Level.Iteration)
        public void doTearDown() {
            sum = 0;
        }
    }

    /*
        value - number of times harness should fork, zero means "no fork"
        warmup - number of times harness should fork and ignore the results
    */
    //@Fork(value = 1, warmups = 2)
    /*
        Parallel threads count
     */
    //@Threads(3)
    /*
        Mode.Throughput - Measures the number of operations per second, meaning the number of times per second your benchmark method could be executed
        Mode.AverageTime - Measures the average time it takes for the benchmark method to execute (a single execution)
        Mode.SampleTime - Measures how long time it takes for the benchmark method to execute, including max, min time etc
        Mode.SingleShotTime - Measures how long time a single benchmark method execution takes to run. This is good to test how it performs under a cold start (no JVM warm up)
        Mode.All- Measures all of the above
    */
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Benchmark
    public void testMethod(MyState state) {
        int a = 1;
        int b = 2;
        for (int i = 0; i < state.iterations; i++)
            state.sum = a + b + state.c;
    }
}
