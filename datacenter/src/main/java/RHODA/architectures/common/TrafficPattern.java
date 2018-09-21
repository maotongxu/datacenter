package RHODA.architectures.common;

import RHODA.architectures.api.DataCenter;
import RHODA.architectures.api.Flow;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class TrafficPattern {
  public static final int ONE_TO_ONE_TRAFFIC = 0;
  public static final int ONE_TO_MULTIPLE_TRAFFIC = 1;
  public static final int MULTIPLE_TO_ONE_TRAFFIC = 2;
  public static final int INCREASING_DESTINATIONS_TRAFFIC = 3;
  public static final int FACEBOOK_TRAFFIC = 4;

  private static final int numOfRacks = Configuration.getInstance().getNumOfRacks();
  private static final Random rand = new Random();

  private final DataCenter dataCenter;

  public TrafficPattern(final DataCenter dataCenter) {
    this.dataCenter = dataCenter;
  }

  public void importTraffic() {
    double[][] flowMatrix = generateFlowMatrix();
    loadTrafficToRacks(flowMatrix);
  }

  public static double[][] generateFlowMatrix() {
    double[][] flowMatrix = new double[numOfRacks][numOfRacks];

    int trafficPattern = Configuration.getInstance().getTrafficPattern();
    if (trafficPattern == ONE_TO_ONE_TRAFFIC) {
      flowMatrix = importOneToOneTraffic();
    } else if (trafficPattern == ONE_TO_MULTIPLE_TRAFFIC) {
      flowMatrix = importOneToMultipleTraffic();
    } else if (trafficPattern == MULTIPLE_TO_ONE_TRAFFIC) {
      flowMatrix = importMultipleToOneTraffic();
    } else if (trafficPattern == INCREASING_DESTINATIONS_TRAFFIC) {
      flowMatrix = importIncreasingDestinationsTraffic();
    } else if (trafficPattern == FACEBOOK_TRAFFIC) {
      flowMatrix = generateFBTraffic();
    }
    System.out.println(Arrays.toString(flowMatrix[0]));
    return flowMatrix;
  }

  private static double[][] importOneToOneTraffic() {
    double[][] flowMatrix = new double[numOfRacks][numOfRacks];
    int interval = 1;
    for (int src = 0; src < numOfRacks; src += interval) {
      int dst = (src + 16) % numOfRacks;
      flowMatrix[src][dst] = 1;
    }
    return flowMatrix;
  }

  private static double[][] importOneToMultipleTraffic() {
    double[][] flowMatrix = new double[numOfRacks][numOfRacks];
    int interval = 1+rand.nextInt(16);
    for (int src=0; src<numOfRacks; ) {
      for (int dst = src + 1; dst < src + interval && dst < numOfRacks; dst++) {
        flowMatrix[src][dst] = 2;
      }
      src += interval;
      interval = 1 + rand.nextInt(16);
    }
    return flowMatrix;
  }

  private static double[][] importMultipleToOneTraffic() {
    double[][] flowMatrix = new double[numOfRacks][numOfRacks];
    int interval = 1 + rand.nextInt(16);
    for (int dst = 0; dst < numOfRacks; dst += interval) {
      for (int src = dst + 1; src < dst + interval && src < numOfRacks; src ++) {
        flowMatrix[src][dst] = 10;
      }
      interval = 1 + rand.nextInt(16);
    }
    return flowMatrix;
  }

  private static double[][] importIncreasingDestinationsTraffic() {
    double[][] flowMatrix = new double[numOfRacks][numOfRacks];

    int src_start = rand.nextInt(numOfRacks);
    float flow_size = (float) 64 / numOfRacks;

    for (int r = 0; r < numOfRacks; r++) {
      int src = (src_start + r) % numOfRacks;
      int dst_start = rand.nextInt(numOfRacks);
      for (int r_n = 0; r_n < r; r_n++) {
        int dst = (dst_start + r_n) % numOfRacks;
        flowMatrix[src][dst] = flow_size;
      }
    }
    return flowMatrix;
  }

  private static double[][] generateFBTraffic() {
    String traceFile = "/home/maotong/workspace/ClusterAClusterBClusterC_Data/"+
        "ClusterADataSummarySortOnTimeFlow1000";

    double[][] flowMatrix = new double[numOfRacks][numOfRacks];
    try {
      BufferedReader br = new BufferedReader(new FileReader(traceFile));
      String line;
      while ((line = br.readLine()) != null) {
        String[] splited = line.split("\\s+");
        int srcId = Math.min(Integer.parseInt(splited[2]), numOfRacks-1);
        int dstId = Math.min(Integer.parseInt(splited[3]), numOfRacks-1);
        double traffic = Double.parseDouble(splited[4])/1e8;
        flowMatrix[srcId][dstId] += traffic;
      }
      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return flowMatrix;
  }

  private void loadTrafficToRacks(final double[][] flowMatrix) {
    for (int src = 0; src < numOfRacks; src++) {
      for (int dst = 0; dst < numOfRacks; dst++) {
        if (src == dst || flowMatrix[src][dst] < Double.MIN_VALUE) {
          continue;
        }
        Flow flow = new Flow(src, dst, flowMatrix[src][dst]);
        dataCenter.getRack(src).addFlow(flow);
      }
    }
  }
}
