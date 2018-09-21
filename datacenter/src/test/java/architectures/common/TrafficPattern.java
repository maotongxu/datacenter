package architectures.common;

import RHODA.architectures.api.DataCenter;
import RHODA.architectures.api.Flow;
import RHODA.architectures.common.Configuration;

import java.util.Random;

public class TrafficPattern {
  private static final double NEGLIGIBLE_FLOW_TRAFFIC = 0.00001;

  private final Random rand = new Random();
  private final DataCenter dataCenter;
  private final int numOfRacks;

  public TrafficPattern(final DataCenter dataCenter) {
    this.dataCenter = dataCenter;
    this.numOfRacks = Configuration.getInstance().getNumOfRacks();
  }

  public void importTrafficToRacks() {
    double[][] trafficFlow = generateTraffic();
    for (int src = 0; src < numOfRacks; src++) {
      for (int dst = 0; dst < numOfRacks; dst++) {
        if (src == dst || trafficFlow[src][dst] < NEGLIGIBLE_FLOW_TRAFFIC) {
          continue;
        }
        Flow flow = new Flow(src, dst, trafficFlow[src][dst]);
        dataCenter.getRack(src).addFlow(flow);
      }
    }
  }

  public static double[][] generateTraffic() {
    int numOfRacks = Configuration.getInstance().getNumOfRacks();
    double[][] traffic = new double[numOfRacks][numOfRacks];

    for (int i = 0; i < numOfRacks; i++) {
      for (int j = 0; j < numOfRacks; j++) {
        if (i == j) {
          continue;
        }
        traffic[i][j] = i + j;
      }
    }
    return traffic;
  }
}
