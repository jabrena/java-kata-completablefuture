package none.cvg.completablefuture;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import static org.assertj.core.api.BDDAssertions.then;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

public class CFBasicsTest {

    @Test
    public void given_CF_when_Call_then_returnExpectedValue() {

        CFBasics example = new CFBasics();

        await()
                .atMost(Duration.ofSeconds(3))
                .until(example::myFirstCF, equalTo(2));
    }

    @Test
    public void given_CF2_when_Call_then_returnExpectedValue() {

        CFBasics example = new CFBasics();

        Callable demo = () -> example.mySecondCF().join();

        await()
                .atMost(Duration.ofSeconds(7))
                .until(demo, equalTo(4));
    }

    @Test
    public void given_CF3_when_Call_then_returnExpectedValue() {

        CFBasics example = new CFBasics();

        Callable demo = () -> example.myThirdCF();

        await()
                .atMost(Duration.ofSeconds(7))
                .until(demo, equalTo(6));
        ;
    }

    @Test
    public void given_CF3_2_when_Call_then_returnExpectedValue() {

        CFBasics example = new CFBasics();

        Callable demo = () -> example.myThirdCF2();

        await()
                .atMost(Duration.ofSeconds(7))
                .until(demo, equalTo(6));
        ;
    }

    @Test
    public void given_CF4_when_Call_then_returnExpectedValue() {

        CFBasics example = new CFBasics();

        Callable demo = () -> example.myForthCF();

        await()
                .atMost(Duration.ofSeconds(7))
                .until(demo, equalTo(4));
    }

    @Test
    public void given_CF5_when_Call_then_returnExpectedValue() throws Exception {

        CFBasics example = new CFBasics();

        then(example.myFifthCF()).isEqualTo(2);
    }

    @Test
    public void given_CF6_when_Call_then_returnExpectedValue() {

        CFBasics example = new CFBasics();

        Callable demo = () -> example.mySixthCF();

        await()
                .atMost(Duration.ofSeconds(7))
                .until(demo, equalTo(102));
    }

    @Test
    public void given_CF7_when_Call_then_returnExpectedValue() {

        CFBasics example = new CFBasics();

        Callable demo = () -> example.mySeventhCF();

        await()
            .atMost(Duration.ofSeconds(7))
            .until(demo, equalTo(0));

        then(example.mySeventhCF()).isEqualTo(0);
    }

    @Test
    public void given_CF8_when_Call_then_returnExpectedValue() {

        CFBasics example = new CFBasics();

        Callable demo = () -> example.myEightCF();

        await()
                .atMost(Duration.ofSeconds(2))
                .until(demo, equalTo("Result of Future 2"));

        then(example.myEightCF()).isEqualTo("Result of Future 2");
    }

    @Test
    public void given_CF9_when_Call_then_returnExpectedValue() {

        CFBasics example = new CFBasics();

        //Callable demo = () -> example.myNinethCF2();

        //await()
        //        .atMost(Duration.ofSeconds(7))
        //        .until(demo, equalTo(1));

        then(example.myNinethCF3()).isEqualTo(8);
    }

    @Test
    public void given_CF9_4_when_Call_then_returnExpectedValue() {

        CFBasics example = new CFBasics();

        //Callable demo = () -> example.myNinethCF2();

        //await()
        //        .atMost(Duration.ofSeconds(7))
        //        .until(demo, equalTo(1));

        then(example.myNinethCF4()).isEqualTo(1);
    }

}