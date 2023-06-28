package fr.aelion.java2306.concurrency;

import java.util.*;
import java.util.concurrent.*;

public class Reactive {

    public static void main(String[] args) {
        //futures();
        // completableFutures();

        // https://www.baeldung.com/java-9-reactive-streams
        System.out.println("do async job");
        Flow.Subscriber<Optional<Integer>> subscriber = new EndSubscriber<>();
        // const observer = { next: , ..., error: ..., complete: ... }
        // observable.subscribe(observer)
        reactive().subscribe(subscriber);
        System.out.println("waiting for results");

    }

    static SubmissionPublisher<Optional<Integer>> reactive() {
        SubmissionPublisher<Optional<Integer>> publisher = new SubmissionPublisher<>();
        // async treatment
        new Thread(createTreatment(publisher)).start();
        return publisher;
    }

    static Runnable createTreatment(SubmissionPublisher publisher) {
        return () -> {
            // return (int) (Math.random()*100);
            try {
                System.out.println();
                for (int i = 0; i < 10; i++) {
                    TimeUnit.SECONDS.sleep(new Random().nextInt(10));
                    publisher.submit(Optional.of(new Random().nextInt(100)));
                }
                publisher.close();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
    }

    static void completableFutures() {
        CompletableFuture<String> completableFuture
                = CompletableFuture.supplyAsync(() -> "Hello");

        CompletableFuture<Void> future = completableFuture
                .thenRun(() -> System.out.println("Computation finished."));

        try {
            System.out.println("Before get");
            future.get();
            System.out.println("After get");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    static void futures() {
        Runnable runnableTask = () -> {
            System.out.println("Hello");
        };
        Callable<String> callableTask = () -> {
            TimeUnit.SECONDS.sleep(10);
            return "Hello from callable";
        };
        List<Callable<String>> callableList = new ArrayList<>();
        callableList.add(callableTask);
        callableList.add(() -> {
            TimeUnit.SECONDS.sleep(3);
            return "Hello from other callable";
        });
        ExecutorService es = Executors.newWorkStealingPool();

        try {
            List<java.util.concurrent.Future<String>> futures = es.invokeAll(callableList);
            System.out.println("Thread is running");
            es.shutdown();
            es.awaitTermination(30, TimeUnit.SECONDS);
            futures.stream().forEach(future -> {
                try {
                    System.out.println(future.get(1, TimeUnit.SECONDS));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (TimeoutException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

class EndSubscriber<Integer> implements Flow.Subscriber<Optional<Integer>> {
    private Flow.Subscription subscription;
    public List<Integer> consumedElements = new LinkedList<>();

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(Optional<Integer> item) {
        System.out.println("Got : " + item.get());
        consumedElements.add(item.get());
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Error");
    }

    @Override
    public void onComplete() {
        System.out.println("Complete");
    }
}
