package org.lkpnotice.infra.headlessbrowser;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TestFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        boolean ss = true;
        System.out.println("rStr : is " );
        CompletableFuture<String> result = CompletableFuture.supplyAsync(() ->
                {

                    while(ss){
                        try {
                            Thread.sleep(30000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            System.out.println("xxxxx ");
                        }
                    }


                    return "done";
                });

        try {
            String rStr = result.get(5, TimeUnit.SECONDS);
            System.out.println("rStr : " + rStr);
        } catch (TimeoutException exception) {
            System.out.println("e " + exception);
            result.cancel(true);

        }
        catch (InterruptedException exception) {
            System.out.println("e " + exception);

        }
        catch (ExecutionException exception) {
            System.out.println("e " + exception);
        }



        while(true){
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("xxxxx ");
            }
        }

    }
}
