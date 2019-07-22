package io.jenkins.plugins.folderauth.jmh;

import jenkins.benchmark.jmh.BenchmarkFinder;
import org.junit.Test;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class BenchmarkRunner {
    @Test
    public void runBenchmarks() throws IOException, RunnerException {
        ChainedOptionsBuilder options = new OptionsBuilder()
                                            .forks(2)
                                            .mode(Mode.AverageTime)
                                            .shouldDoGC(true)
                                            .shouldFailOnError(true)
                                            .result("jmh-report.json")
                                            .resultFormat(ResultFormatType.JSON)
                                            .timeUnit(TimeUnit.MICROSECONDS)
                                            .threads(2);

        new BenchmarkFinder(BenchmarkRunner.class).findBenchmarks(options);
        new Runner(options.build()).run();
    }
}
