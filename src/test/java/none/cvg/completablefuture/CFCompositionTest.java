package none.cvg.completablefuture;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.assertj.core.api.BDDAssertions.then;

@Slf4j
public class CFCompositionTest {

    private Integer method1(Integer param) {

        LOGGER.info("Thread: {}", Thread.currentThread().getName());

        delay(3);

        return 1 + param;
    }

    private Integer method2(Integer param) {

        LOGGER.info("Thread: {}", Thread.currentThread().getName());

        delay(1);

        return 1 + param;
    }

    private void delay(int seconds) {
         Try.run(() -> Thread.sleep(seconds * 1000));
    }

    private CompletableFuture<Integer> cf(Integer param)  {

        return new CompletableFuture<>()
                .supplyAsync(() -> 1 + param)
                .handle((result, ex) -> {
                    if(!Objects.isNull(ex)) {
                        LOGGER.info("{}", 99);
                        return 99;
                    }
                    LOGGER.info("{}", result);
                    return result;
                });
    }

    @Test
    public void composeTest() {

        then(this.cf(1)
                .thenCompose(cfResult -> cf(cfResult))
                .thenCompose(cfResult2 -> cf(cfResult2))
                .join())
                .isEqualTo(4);
    }

    Function<Integer, CompletableFuture<Integer>> cf1 = (param) ->  {

        return new CompletableFuture<>()
                .supplyAsync(() -> method1(param))
                .handle((result, ex) -> {
                    if(!Objects.isNull(ex)) {
                        LOGGER.info("{}", 99);
                        return 99;
                    }
                    LOGGER.info("{}", result);
                    return result;
                });
    };

    @Test
    public void composeTest2() {

        then(cf1.apply(1)
                .thenCompose(cfResult -> cf1.apply(cfResult))
                .thenCompose(cfResult2 -> cf1.apply(cfResult2))
                .join())
                .isEqualTo(4);
    }

    @Test
    public void composeTest3() {

        CompletableFuture<Integer> cf2 = new CompletableFuture<>()
                .supplyAsync(() -> 1)
                .handle((result, ex) -> {
                    if(!Objects.isNull(ex)) {
                        LOGGER.info("{}", 99);
                        return 99;
                    }
                    LOGGER.info("{}", result);
                    return result;
                });

        then(cf1.apply(1)
                .thenCompose(cfResult -> cf1.apply(cfResult))
                .thenCompose(cfResult2 -> cf2)
                .join())
                .isEqualTo(1);
    }

    CompletableFuture<Void> executeAsync(List<Integer> items) {
        return items.stream()
                .map(this::cf)
                .reduce(completedFuture(null), (y, x) -> x.thenCompose(r -> y.thenCompose(ignored -> cf(r))))
                .thenRun(() -> {});

    }

    @Test
    public void testReduce() {

        var list = List.of(1,2,3);
        executeAsync(list).join();
    }

    @Test
    public void combineTest(){

        CompletableFuture<String> completableFuture = CompletableFuture
                .supplyAsync(() -> "Hello")
                .thenCombine(CompletableFuture.supplyAsync(
                        () -> " World"), (s1, s2) -> s1 + s2);

        then(completableFuture.join()).isEqualTo("Hello World");
    }

}