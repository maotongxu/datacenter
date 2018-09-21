package RHODA.architectures.output;

import RHODA.architectures.common.Configuration;
import RHODA.architectures.api.Path;
import RHODA.architectures.api.SrcDstPair;

import java.util.HashMap;
import java.util.Map;

public class Metrics {

  public static Map<SrcDstPair, FlowInfo> srcDstPairFlowInfoMap = new HashMap<>();

  public static Map<Integer, Double> rackIdRackLoadMap = new HashMap<>();

  public static void init() {
    srcDstPairFlowInfoMap.clear();
    rackIdRackLoadMap.clear();
  }

  public static void addToOuptut(final SrcDstPair srcDstPair, final Path path, final double traffic) {
    if (Metrics.srcDstPairFlowInfoMap.containsKey(srcDstPair)) {
      FlowInfo flowInfo = Metrics.srcDstPairFlowInfoMap.get(srcDstPair);
      flowInfo.setNumOfHops(flowInfo.getNumOfHops() + path.pathNodeIdListSize() - 1);
    } else {
      FlowInfo flowInfo = new FlowInfo(traffic, path.pathNodeIdListSize() - 1);
      Metrics.srcDstPairFlowInfoMap.put(srcDstPair, flowInfo);
    }

    for (int i = 0; i < path.pathNodeIdListSize(); i++) {
      int rackId = path.getPathNodeId(i);
      Metrics.rackIdRackLoadMap.put(rackId, Metrics.rackIdRackLoadMap.getOrDefault(rackId, 0.0) + traffic);
    }
  }

  public static double calculateTotalTraffic() {
    double totalTraffic = 0;
    for (FlowInfo flowInfo : srcDstPairFlowInfoMap.values()) {
      totalTraffic += flowInfo.getTraffic();
    }
    return totalTraffic;
  }

  public static double calculateAvgNumOfHops() {
    double totalTrafficTimesNumOfHops = 0;
    double totalTraffic = 0;
    for (FlowInfo flowInfo : srcDstPairFlowInfoMap.values()) {
      double trafficTimesNumOfHops = flowInfo.getTraffic() * flowInfo.getNumOfHops();
      totalTrafficTimesNumOfHops += trafficTimesNumOfHops;
      totalTraffic += flowInfo.getTraffic();
    }
    return totalTrafficTimesNumOfHops / totalTraffic;
  }

  public static double calculateAvgRackLoad() {
    double totalLoad = 0;
    for (double load : rackIdRackLoadMap.values()) {
      totalLoad += load;
    }
    return totalLoad / Configuration.getInstance().getNumOfRacks();
  }
}
