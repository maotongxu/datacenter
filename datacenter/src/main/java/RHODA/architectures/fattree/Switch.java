package RHODA.architectures.fattree;

import RHODA.architectures.output.FlowInfo;
import RHODA.architectures.output.Metrics;

import java.util.LinkedList;
import java.util.Queue;

public class Switch {
  public final static int CORE_SWITCH = 0;
  public final static int AGG_SWITCH = 1;
  public final static int EDGE_SWITCH = 2;

  private final Queue<FlowFT> buffer;

  private int switchType;
  private Address address;

  private int switchId1;
  private int switchId2;

  private int portDown;
  private int portUp;

  private int surPort;

  private double loadSW;

  public Switch() {
    buffer = new LinkedList<>();
    address = new Address();
  }

  public void rxFlow(final FlowFT flowFT) {
    buffer.add(flowFT);
  }

  public void txFlow() {
    if (buffer.isEmpty()) {
      return;
    }

    while (!buffer.isEmpty()) {
      FlowFT flowFT = buffer.poll();

      flowFT.incrNumOfHops();
      loadSW += flowFT.getTrafficUnit();

      if (switchType == EDGE_SWITCH) {
        if (address.getAddress2() == flowFT.getAddressDst().getAddress2() &&
            address.getAddress3() == flowFT.getAddressDst().getAddress3()) {
          // Tx to a host
          FlowInfo flowInfo = new FlowInfo(flowFT.getTrafficUnit(), flowFT.getNumOfHops());

          Metrics.srcDstPairFlowInfoMap.put(flowFT.getSrcDstPair(), flowInfo);

        } else {
          Scheduler.sw_agg[switchId1][portUp % (ConfigurationFT.K / 2)].rxFlow(flowFT);
          portUp++;
        }
      } else if (switchType == AGG_SWITCH) {
        if (address.getAddress2() == flowFT.getAddressDst().getAddress2()) {
          Scheduler.sw_edge[switchId1][flowFT.getAddressDst().getAddress3()].rxFlow(flowFT);
          portDown++;
        } else {
          Scheduler.sw_core[switchId2][surPort - ConfigurationFT.K / 2].rxFlow(flowFT);
          portUp++;
        }
      } else if (switchType == CORE_SWITCH) {
        Scheduler.sw_agg[flowFT.getAddressDst().getAddress2()][switchId1].rxFlow(flowFT);
        portDown++;
      } else {
        throw new IllegalArgumentException("No such switch " + switchType);
      }
    }
  }

  public Queue<FlowFT> getBufferedFlows() {
    return buffer;
  }

  public int getSwitchType() {
    return switchType;
  }

  public void setSwitchType(int switchType) {
    this.switchType = switchType;
  }

  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public int getSwitchId1() {
    return switchId1;
  }

  public void setSwitchId1(int switchId1) {
    this.switchId1 = switchId1;
  }

  public int getSwitchId2() {
    return switchId2;
  }

  public void setSwitchId2(int switchId2) {
    this.switchId2 = switchId2;
  }

  public int getSurPort() {
    return surPort;
  }

  public void setSurPort(int surPort) {
    this.surPort = surPort;
  }

  public double getLoadSW() {
    return loadSW;
  }
}
