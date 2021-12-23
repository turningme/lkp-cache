package org.lkpnotice.infra.headlessbrowser;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// org.lkpnotice.infra.headlessbrowser.TestFuture
public class TestFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        boolean ss = true;
        System.out.println("rStr : is " );
        CompletableFuture<String> result = CompletableFuture.supplyAsync(() ->
                {

                    for(int i = 0; i< 2 ; i++){
                        try {
                            System.out.println(" now in i= " + i);
                            Thread.sleep(30000);
                            System.out.println(" now after in i= " + i);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            System.out.println("interrupted now ");
                        }
                    }


                    return "done";
                });

        try {
            String rStr = result.get(5, TimeUnit.SECONDS);
            System.out.println("rStr : " + rStr);
        } catch (TimeoutException exception) {
            System.out.println("e " + exception);
            for(int i = 0;i <10 ; i++){
                    result.cancel(true);
                    System.out.println("cancel times  i= " + i);
                    try {
                        Thread.sleep(200);
                    }catch (Exception e){
                        System.out.println(" sleep 200 exception " + e);
                    }

            }


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
