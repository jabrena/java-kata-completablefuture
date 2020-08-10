package none.cvg.completablefuture;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * References
 * https://www.nurkiewicz.com/2013/05/java-8-definitive-guide-to.html
 * http://millross-consultants.com/completable-future-error-propagation.html
 *
 */
@Slf4j
public class CFBasics {

    Function<Integer, Integer> compute = param -> {

        LOGGER.info("Compute {}", param);

        Delay(2);

        return param + 1;
    };

    //Simple usage of completablefuture
    public Integer myFirstCF() {
        return asyncCall(1).join();
    }

    CompletableFuture<Integer> asyncCall(Integer param) {
        CompletableFuture<Integer> cf1 = CompletableFuture
                .supplyAsync(() -> this.compute.apply(param));
        return cf1;
    }

    CompletableFuture<Optional<Integer>> asyncCallOptional(Integer param) {
        CompletableFuture<Optional<Integer>> cf1 = CompletableFuture
                .supplyAsync(() -> Optional.of(this.compute.apply(param)));
        return cf1;
    }

    CompletableFuture<Integer> asyncCallFailed() {
        CompletableFuture<Integer> cf1 = CompletableFuture
                .supplyAsync(() -> {
                    throw new RuntimeException("Katakroker");
                });
        return cf1;
    }

    CompletableFuture<Integer> asyncCallFailedProtected() {
        CompletableFuture<Integer> cf1 = CompletableFuture
                .supplyAsync(() -> {
                    throw new RuntimeException("Katakroker");
                })
                .handle((result, ex) -> {
                    return 100;
                });
        return cf1;
    }

    Supplier<Integer> katakroker = () -> {
        Delay(1);
        throw new RuntimeException("Katakroker");
    };

    CompletableFuture<Optional<Integer>> asyncCallFailedOptional() {
        CompletableFuture<Optional<Integer>> cf1 = CompletableFuture
                .supplyAsync(() -> Optional.of(katakroker.get()));
        return cf1;
    }

    private static void Delay(Integer seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //CF Composition
    public CompletableFuture<Integer> mySecondCF() {
        int initialValue = 1;
        CompletableFuture<Integer> completableFuture = asyncCall(initialValue)
                .thenCompose(i -> asyncCall(i))
                .thenCompose(i -> asyncCall(i));

        return completableFuture;
    }

    //Async Calls in parallel
    public Integer myThirdCF() {

        CompletableFuture<Integer> request1 = asyncCall(1);
        CompletableFuture<Integer> request2 = asyncCall(1);
        CompletableFuture<Integer> request3 = asyncCall(1);

        List<CompletableFuture<Integer>> futuresList = List.of(request1, request2, request3);

        return futuresList.stream()
                .map(CompletableFuture::join)
                .reduce(0 , (i1, i2) -> i1 + i2);
    }

    public Integer myThirdCF2() {

        CompletableFuture<Integer> request1 = asyncCall(1);
        CompletableFuture<Integer> request2 = asyncCall(1);
        CompletableFuture<Integer> request3 = asyncCall(1);

        return List.of(request1, request2, request3).stream()
                .map(CompletableFuture::join)
                .reduce(0 , (i1, i2) -> i1 + i2);
    }

    public Integer myForthCF() {

        CompletableFuture<Integer> request1 = asyncCall(1);
        CompletableFuture<Integer> request2 = asyncCallFailed();
        CompletableFuture<Integer> request3 = asyncCall(1);

        List<CompletableFuture<Integer>> futuresList = List.of(request1, request2, request3);

        return futuresList.stream()
                .filter(cf -> !cf.isCompletedExceptionally())
                .map(CompletableFuture::join)
                .reduce(0 , (i1, i2) -> i1 + i2);
    }

    /**
     * If an exception occurs in a stage and we do not do anything
     * to handle that exception then execution to the further stages is abandon.
     *
     * @return
     */
    public Integer myFifthCF() {

        System.out.println("Defining");

        CompletableFuture<Integer> request1 = asyncCall(1);
        CompletableFuture<Integer> request2 = asyncCallFailed();
        CompletableFuture<Integer> request3 = asyncCallFailed();
        CompletableFuture<Integer> request4 = asyncCallFailed();

        System.out.println("Running");

        var futuresList2 = Stream.of(request1, request2, request3, request4);

        return futuresList2
                .filter(not(CompletableFuture::isCompletedExceptionally))
                .map(CompletableFuture::join)
                .reduce(0 , (i1, i2) -> i1 + i2);
    }

    public Integer mySixthCF() {

        System.out.println("Defining");

        CompletableFuture<Integer> request1 = asyncCall(1);
        //CompletableFuture<Integer> request2 = asyncCallFailed();
        CompletableFuture<Integer> request3 = asyncCallFailedProtected();

        List<CompletableFuture<Integer>> futuresList = List.of(request1, request3);

        System.out.println("Running");

        return futuresList.stream()
                //.filter(not(CompletableFuture::isCompletedExceptionally))
                .map(CompletableFuture::join)
                .reduce(0 , (i1, i2) -> i1 + i2);
    }

    public Integer mySeventhCF() {

        System.out.println("Defining");

        CompletableFuture<Optional<Integer>> request1 = asyncCallOptional(1);
        CompletableFuture<Optional<Integer>> request2 = asyncCallFailedOptional();
        CompletableFuture<Optional<Integer>> request3 = asyncCallFailedOptional();
        CompletableFuture<Optional<Integer>> request4 = asyncCallFailedOptional();

        List<CompletableFuture<Optional<Integer>>> futuresList = List.of(request1, request2, request3, request4);

        System.out.println("Running");

        return futuresList.stream()
                .filter(CompletableFuture::isCompletedExceptionally)
                .map(CompletableFuture::join)
                .map(o -> o.get())
                .reduce(0 , (i1, i2) -> i1 + i2);
    }

    //Anyof
    CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
        return "Result of Future 1";
    });

    CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
        return "Result of Future 2";
    });

    CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
        return "Result of Future 3";
    });

    public String myEightCF() {

        CompletableFuture<Object> anyOfFuture = CompletableFuture.anyOf(future1, future2, future3);

        return (String) anyOfFuture.join(); // Result of Future
    }

    public Integer myNinethCF() {

        CompletableFuture<Integer> request1 = asyncCall(1);
        CompletableFuture<Integer> request2 = asyncCallFailed();
        CompletableFuture<Integer> request3 = asyncCall(1);

        List<CompletableFuture<Integer>> futuresList = List.of(request1, request2, request3);


        // Create a combined Future using allOf()
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futuresList.toArray(new CompletableFuture[futuresList.size()])
        );

        // When all the Futures are completed, call `future.join()` to get their results and collect the results in a list -
        CompletableFuture<List<Integer>> allPageContentsFuture = allFutures.thenApply(v -> {
            return futuresList.stream()
                    .map(pageContentFuture -> pageContentFuture.join())
                    .collect(Collectors.toList());
        });

        return allPageContentsFuture.join().stream().reduce(0 , (i1, i2) -> i1 + i2);
    }

    public Integer myNinethCF2() {
        StringBuilder result = new StringBuilder();
        List<String> messages = Arrays.asList("a", "b", "c");
        List<CompletableFuture<String>> futures = messages.stream()
                .map(msg -> CompletableFuture.completedFuture(msg).thenApply(s -> delayedUpperCase(s)))
                .collect(Collectors.toList());
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]))
                .whenComplete((v, th) -> {
            //futures.forEach(cf -> assertTrue(isUpperCase(cf.getNow(null))));
            result.append("done");
        });
        assertTrue(result.length() > 0, "Result was empty");

        return 1;
    }

    private String delayedUpperCase(String s) {
        String result = "";
        try {
            Thread.sleep(1 * 1000);
            result = s.toUpperCase();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }

    public Integer myNinethCF3() {

        long beginTime = System.currentTimeMillis();
        CompletableFuture<Integer> request1 = asyncCall(1);
        CompletableFuture<Integer> request2 = asyncCall(1);
        CompletableFuture<Integer> request3 = asyncCall(1);
        CompletableFuture<Integer> request4 = asyncCall(1);


        CompletableFuture<Integer> allInfoFuture = CompletableFuture.allOf(request1, request2, request3, request4)
                .thenApply(i -> {
                    Integer result1 = request1.join();
                    Integer result2 = request2.join();
                    Integer result3 = request3.join();
                    Integer result4 = request4.join();

                    return result1 + result2 + result3 + result4;
                });
        Integer allInfo = allInfoFuture.join();
        System.out.println(allInfo + "\r\ntime cost:" + (System.currentTimeMillis() - beginTime));

        return allInfo;
    }

    public Integer myNinethCF4() {

        CompletableFuture<Integer> request1 = asyncCall(1);
        CompletableFuture<Integer> request2 = asyncCall(1);
        CompletableFuture<Integer> request3 = asyncCall(1);
        CompletableFuture<Integer> request4 = asyncCall(1);

        StringBuilder result = new StringBuilder();
        List<CompletableFuture<Integer>> futures = List.of(request1, request2, request3, request4).stream()
                .collect(Collectors.toList());
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]))
                .whenComplete((v, th) -> {
                    //futures.forEach(cf -> cf.getNow(null));
                    System.out.println("done");
                });
        allOf.join();

        return 1;
    }


}
