package org.lkpnotice.infra.reactiveSteamAkka.custom;

import akka.stream.Attributes;
import akka.stream.Outlet;
import akka.stream.SourceShape;
import akka.stream.stage.AbstractOutHandler;
import akka.stream.stage.GraphStage;
import akka.stream.stage.GraphStageLogic;

public class NumbersSource extends GraphStage<SourceShape<Integer>> {
  // Define the (sole) output port of this stage
  public final Outlet<Integer> out = Outlet.create("NumbersSource.out");

  // Define the shape of this stage, which is SourceShape with the port we defined above
  private final SourceShape<Integer> shape = SourceShape.of(out);

  @Override
  public SourceShape<Integer> shape() {
    return shape;
  }

  // This is where the actual (possibly stateful) logic is created
  @Override
  public GraphStageLogic createLogic(Attributes inheritedAttributes) {
    return new GraphStageLogic(shape()) {
      // All state MUST be inside the GraphStageLogic,
      // never inside the enclosing GraphStage.
      // This state is safe to access and modify from all the
      // callbacks that are provided by GraphStageLogic and the
      // registered handlers.
      private int counter = 1;

      {
        setHandler(
            out,
            new AbstractOutHandler() {
              @Override
              public void onPull() throws Exception {
                push(out, counter);
                counter += 1;
              }
            });
      }
    };
  }
}