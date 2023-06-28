package fr.aelion.java2306.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class Main {

    static List<Integer> numbers = new ArrayList<>();
    static AtomicLong sum = new AtomicLong(0);

    public static void main(String[] args) {
        initNumbers();
        // run without mutlithreading
        // simpleRun();
        // parallelStreams();
        // parallelThreadClass();
        parallelExecutors();
    }

    private static void initNumbers() {
        IntStream.range(0, 500_000).forEach(numbers::add);
    }

    private static void simpleRun() {
        long start = System.nanoTime();
        long sum = 0L;
        for(int n : numbers) {
            sum += n;
        }
        long stop = System.nanoTime();
        System.out.println("simple run " + (stop - start));
    }

    private static void parallelStreams() {
        long start = System.nanoTime();
        // Executor => new CachedThreadPool
        long sum = numbers.parallelStream().mapToLong(Integer::intValue).sum();
        long stop = System.nanoTime();
        System.out.println("parallelStreams run " + (stop - start));
    }

    private static void parallelThreadClass() {
        List<Integer> part1 = numbers.subList(0, numbers.size() / 2);
        List<Integer> part2 = numbers.subList(numbers.size() / 2 + 1, numbers.size());
        AtomicLong sum = new AtomicLong(0L);
        long start = System.nanoTime();
        Thread t1 = new Thread(() -> part1.forEach(n -> sum.addAndGet(n)));
        t1.start();
        Thread t2 = new Thread(() -> part2.forEach(n -> sum.addAndGet(n)));
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        long stop = System.nanoTime();
        System.out.println("parallelThreadClass run " + (stop - start));
    }

    private static void parallelExecutors() {
        int cpus = Runtime.getRuntime().availableProcessors();
        System.out.println("cpus : " + cpus);
        // cpus = 4;
        // ExecutorService es = Executors.newFixedThreadPool(cpus);
        ExecutorService es = Executors.newWorkStealingPool();

        int limit = numbers.size() / cpus;
        long start = System.nanoTime();
        for(int i = 0, startCount = 0; i < cpus; i++, startCount += limit ) {
            // We can use a classic Thread
            // new Thread(new SumCalculator(start, limit)).start();
            // With Executors
            es.submit(new SumCalculator(startCount, limit));
        }
        try {
            // close the pool
            es.shutdown();
            // await current tasks
            es.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        long stop = System.nanoTime();
        System.out.println("parallelExecutors run " + (stop - start));
    }
}


class SumCalculator implements Runnable {

    private int start;
    private int limit;

    public SumCalculator(int start, int limit) {
        this.start = start;
        this.limit = limit;
    }

    @Override
    public void run() {
        long subTotal = 0L;
        for(int i = start, j = 0; j < limit; i++, j++) {
            subTotal += Main.numbers.get(i);
        }
        System.out.println(Thread.currentThread() + " - subTotal is " + subTotal);
        Main.sum.addAndGet(subTotal);
    }
}