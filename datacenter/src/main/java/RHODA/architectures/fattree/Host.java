package RHODA.architectures.fattree;

import java.util.LinkedList;
import java.util.Queue;

public class Host {
  private Address address;

  private final Queue<FlowFT> buffer;

  public Host() {
    address = new Address();
    buffer = new LinkedList<>();
  }

  public void addFlow(final FlowFT flowFT) {
    buffer.add(flowFT);
  }

  public void txFlow() {
    while (!buffer.isEmpty()) {
      FlowFT flowFT = buffer.poll();
      int podId = address.getAddress2();
      int edgeSWId = address.getAddress3();
      flowFT.incrNumOfHops();
      Scheduler.sw_edge[podId][edgeSWId].rxFlow(flowFT);
    }
  }

  public Address getAddress() {
    return address;
  }

  public Queue<FlowFT> getBufferedFlows() {
    return buffer;
  }
}
