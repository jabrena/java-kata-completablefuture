package none.cvg.completablefuture;

import java.util.concurrent.CompletableFuture;

public class CompletableFutureTest {

    public static void main(String[] args) {

        Runnable dummyTask = () -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
            }
        };

        CompletableFuture<Void> f1 = CompletableFuture.runAsync(dummyTask);
        CompletableFuture<Void> f2 = CompletableFuture.runAsync(dummyTask);
        f1 = f1.whenComplete((aVoid, throwable) -> System.out.println("Completed f1"));
        f2 = f2.whenComplete((aVoid, throwable) -> System.out.println("Completed f2"));
        CompletableFuture[] all = {f1, f2};
        CompletableFuture<Void> allOf = CompletableFuture.allOf(all);
        allOf.whenComplete((aVoid, throwable) -> {
            System.out.println("Completed allOf");
        });
        allOf.join();
        System.out.println("Joined");
    }

    /*
    public void completableFutureThenApplyAccept() {
        CompletableFuture.supplyAsync(this::findAccountNumber)
                .thenApply(this::calculateBalance)
                .thenApply(this::notifyBalance)
                .thenAccept((i)->notifyByEmail()).join();
    }

     */
}